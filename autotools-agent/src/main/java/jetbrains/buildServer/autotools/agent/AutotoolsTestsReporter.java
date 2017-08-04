package jetbrains.buildServer.autotools.agent;

import java.io.*;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.log.Loggers;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 02.08.2017.
 * Author     : Nadezhda Demina
 */
public final class AutotoolsTestsReporter {
  /**
   * Current buildLogger
   */
  private BuildProgressLogger myLogger;
  /**
   * Time of current build runner start
   */
  private final long myTimeBeforeStart;
  /**
   * .log files with test results
   */
  private final Map<String, File> myTestsLogFiles;
  /**
   * .trs files with test results
   */
  private final Map<String, File> myTestsTrsFiles;

  private final List<File> myTestsXmlFiles;
  /**
   * Path of checkout directory
   */
  private final String MySrcPath;
  private Boolean hasDejagnu;
  private long myTimeXml;
  /**
   * Constant array with values of success TestResults
   */
  private final static String successTestResults[] = {"PASS", "XFAIL"};

  /**
   * Constant array with values of fail TestResults
   */
  private final static String failTestResults[] = {"XPASS", "FAIL", "ERROR", "UNRESOLVED"};
  /**
   *Constant array with values of skip TestResults
   */
  private final static String skipTestResults[] = {"SKIP", "UNTESTED",  "UNSUPPORTED", "WARNING", "NOTE"};


  public AutotoolsTestsReporter(@NotNull final long timeBeforeStart, @NotNull final BuildProgressLogger logger, @NotNull final String srcPath){
    myTimeBeforeStart = timeBeforeStart;
    myLogger = logger;
    MySrcPath = srcPath;
    myTimeXml = 0;
    myTestsLogFiles = new HashMap<String, File>();
    myTestsTrsFiles = new HashMap<String, File>();
    myTestsXmlFiles = new ArrayList<File>();
    hasDejagnu = false;
  }

  private boolean hasDejagnuOpt(@NotNull final File srcDir){
    try {
      final String entireFileText = new Scanner(srcDir).useDelimiter("\\A").next();
      return entireFileText.contains("dejagnu") || entireFileText.contains("DEJATOOL") || entireFileText.contains("DEJAGNU");
    }
    catch (IOException e){
      return false;
    }
  }
  public void findDejagnu(@NotNull final File srcDir){
    if (hasDejagnu) return;
    for (final File f : srcDir.listFiles()){
      if (f.isDirectory()){
        findDejagnu(f);
      }
      if (isThisExtensionFile(f, ".am") && hasDejagnuOpt(f)){
        hasDejagnu = true;
        return;
      }
    }
  }
  /**
   * Returns True if this file has this extension.
   * @param file File, should be checked
   * @param extension Extension with "."
   * @return true if this file has this extension, else false
   */
  private static boolean isThisExtensionFile(@NotNull final File file, @NotNull final String extension){
    final String fileName = file.getName();
    final int dotIdx = fileName.lastIndexOf(".");
    if (dotIdx == -1){
      return false;
    }
    final String fileExtension = fileName.substring(dotIdx);
    return fileExtension.equalsIgnoreCase(extension);
  }


  /**
   * Finds files, created after execution test, and contains test-results and put their in maps.
   * @param srcDir Directory, where will search files
   */
  @NotNull
  private String getRelativePath(@NotNull final String path){
    return path.replaceFirst(MySrcPath, "");
  }
  public void searchTestsFiles(@NotNull final File srcDir){
    for (final File file : srcDir.listFiles()){
      if (file.isDirectory()){
        searchTestsFiles(file);
      }
      if (isThisExtensionFile(file, ".trs")){
        final String testName = getRelativePath(file.getAbsolutePath()).replace(".trs", "");
        if (file.lastModified() >= myTimeBeforeStart) {
          myTestsTrsFiles.put(testName, file);
        }
        else{
          myLogger.warning("Make check: Test result file " + file.getPath() + " was modifed before build step.");
        }
      }

      if (isThisExtensionFile(file, ".log")) {
        final String testName = file.getAbsolutePath().replace(".log", "").replaceFirst(MySrcPath, "");
        if (file.lastModified() >= myTimeBeforeStart) {
          myTestsLogFiles.put(testName, file);
        }
      }

      if (hasDejagnu && isThisExtensionFile(file, ".xml") && file.lastModified() >= myTimeBeforeStart){
        myTestsXmlFiles.add(file);
      }
    }

  }

  /**
   * Handles all tests results files.
   */
  public void doTestsReport(){
    for (final Map.Entry<String, File> entry : myTestsTrsFiles.entrySet()){
      parseTrsTestResults(entry.getKey(), entry.getValue());
    }

    for (final File file : myTestsXmlFiles){
      parseXmlTestResults(file);
    }
  }

  /**
   * Parse results test testName what is readed from trsFile.
   * @param testName name of test
   * @param trsFile test result file
   * @throws IOException
   */
  private void  parseTrsTestResults(@NotNull final String testName, @NotNull final File trsFile){
    try {
      final BufferedReader bufferead = new BufferedReader(new FileReader(trsFile));
      String str;
      boolean testFailed = false;

      // Parse each string in result file
      while ((str = bufferead.readLine()) != null) {
        int containsIdx = str.indexOf(":test-result:");
        if (containsIdx == -1) {
          continue;
        }
        containsIdx += ":test-result:".length() + 1;
        String res = "";

        // if result of test length 5 such as XPASS XFAIL ERROR if-expression resurns true
        if (containsIdx + 5 <= str.length() && Character.isLetter(str.charAt(containsIdx + 4))) {
          res = str.substring(containsIdx, containsIdx + 5);
        }
        // else result of test length 4 sush as PASS FAIL SKIP
        else {
          res = str.substring(containsIdx, containsIdx + 4);
        }
        publicTestCaseInfo(testName, res, "");
        if (Arrays.asList(failTestResults).contains(res)) {
          testFailed = true;
        }
      }

      if (testFailed && myTestsLogFiles.containsKey(testName)) {
        myLogger.message("##teamcity[publishArtifacts '" + myTestsLogFiles.get(testName) + "']");
      }
    }
    catch (IOException e){
      Loggers.AGENT.warn("AutotoolsLifeCycleListener: In doTestReport method can't read test result file " + trsFile.getAbsolutePath() + ". " + e.getMessage());
    }
  }

  /**
   *  Parse results tests what is readed from xmlFile.
   * @param xmlFile
   */


  private void parseXmlTestResults(@NotNull final File xmlFile){
    Boolean isTestXml = false;
    final String testSuiteName = getRelativePath(xmlFile.getAbsolutePath());
    try {
      final XMLInputFactory f = XMLInputFactory.newInstance();
      final XMLStreamReader reader = f.createXMLStreamReader(new FileReader(xmlFile));
      String testResult = null;
      String testName = null;
      String tempTag = "";
      String testOutput = "";
      while (reader.hasNext()){
        if (!isTestXml && reader.hasName()){
          if (!reader.getLocalName().equalsIgnoreCase("testsuite")){
            break;
          }
          isTestXml = true;
          myLogger.logSuiteStarted(testSuiteName);
        }
        if (reader.isStartElement() && reader.hasName() && reader.getLocalName().equalsIgnoreCase("test")){
          testResult = null;
          testName = null;
        }
        if (reader.isEndElement() && reader.hasName() && reader.getLocalName().equalsIgnoreCase("test")){
          if (testResult != null && testName != null){
            publicTestCaseInfo(testName, testResult, testOutput);
          }
        }

        if (reader.hasName()){
          tempTag = reader.getLocalName();
        }
        if (reader.hasText()){
          if (tempTag.equalsIgnoreCase("result")){
            testResult = reader.getText();
          }
          else {
            if (tempTag.equalsIgnoreCase("name")) {
              testName = reader.getText();
            } else if (tempTag.equalsIgnoreCase("output")) {
              testOutput = reader.getText();
            }
          }
        }
        reader.next();
      }
     // myLogger.message("Finished parse " + xmlFile.getName());
      reader.close();
    } catch (FileNotFoundException e) {
      myLogger.warning("AutotoolsTestRepoter: parseXmlTestsResults exception " + e.getMessage());
      Loggers.AGENT.warn("AutotoolsTestRepoter: parseXmlTestsResults exception " + e.getMessage());
    } catch (XMLStreamException e) {
      myLogger.warning("AutotoolsTestRepoter: parseXmlTestsResults exception " + e.getMessage());
      Loggers.AGENT.warn("AutotoolsTestRepoter: parseXmlTestsResults exception " + e.getMessage());
    }
    catch (Exception e){
      myLogger.warning("AutotoolsTestRepoter: parseXmlTestsResults exception " + e.getMessage());
      Loggers.AGENT.warn("AutotoolsTestRepoter: parseXmlTestsResults exception " + e.getMessage());
    }
    finally {
      if (isTestXml){
        myLogger.logSuiteFinished(testSuiteName);
      }
    }
  }

  /**
   * Public result of testcase on test testName
   * @param testName name of test
   * @param result result of test
   */
  private void publicTestCaseInfo(@NotNull final String testName,@NotNull final String result, @NotNull final String stdOut){
    myLogger.logTestStarted(testName);
    if (Arrays.asList(failTestResults).contains(result)){
      myLogger.logTestFailed(testName, "Failed", result);
    }
    else
      if (!Arrays.asList(successTestResults).contains(result)){
        myLogger.logTestIgnored(testName, result);
      }
    if (stdOut != "") myLogger.logTestStdOut(testName, stdOut);
    myLogger.logTestFinished(testName);
  }
}
