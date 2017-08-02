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


  /**
   * Constant array with values of success TestResults
   */
  private final static String successTestResults[] = {"PASS", "XFAIL"};

  /**
   * Constant array with values of fail TestResults
   */
  private final static String failTestResults[] = {"XPASS", "FAIL", "ERROR"};
  /**
   *Constant array with values of skip TestResults
   */
  private final static String skipTestResults[] = {"SKIP", "UNTESTED", "UNRESOLVED", "UNSUPPORTED", "WARNING", "NOTE"};

  public AutotoolsTestsReporter(@NotNull final long timeBeforeStart, @NotNull final BuildProgressLogger logger, @NotNull final String srcPath){
    myTimeBeforeStart = timeBeforeStart;
    myLogger = logger;
    MySrcPath = srcPath;
    myTestsLogFiles = new HashMap<String, File>();
    myTestsTrsFiles = new HashMap<String, File>();
    myTestsXmlFiles = new ArrayList<File>();
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
  public void searchTestsFiles(@NotNull final File srcDir){
    for (final File file : srcDir.listFiles()){
      if (file.isDirectory()){
        searchTestsFiles(file);
      }
      if (isThisExtensionFile(file, ".trs")){
        final String testName = file.getAbsolutePath().replace(".trs", "").replaceFirst(MySrcPath, "");;
        if (file.lastModified() >= myTimeBeforeStart) {
          myTestsTrsFiles.put(testName, file);
        }
        else{
          myLogger.warning("Make check: Test result file " + file.getPath() + " was modifed before build step.");
        }
      }

      if (isThisExtensionFile(file, ".xml")){
        myTestsXmlFiles.add(file);
      }
    }

    for (final File file : srcDir.listFiles()) {
      if (file.isDirectory()) {
        searchTestsFiles(file);
      }

      if (isThisExtensionFile(file, ".log")) {
        final String testName = file.getAbsolutePath().replace(".log", "").replaceFirst(MySrcPath, "");

        if (!myTestsTrsFiles.containsKey(testName)) {
          continue;
        }

        if (file.lastModified() >= myTimeBeforeStart) {
          myTestsLogFiles.put(testName, file);
        }
        else{
          myLogger.warning("Make check: Test result log file " + file.getPath() + " + was modifed before build step.");
        }
      }
    }
  }

  /**
   * Handles all tests results files.
   */
  public void doTestsReport(){
    myLogger.message("I was this");
    for (final Map.Entry<String, File> entry : myTestsTrsFiles.entrySet()){
      parseTrsTestResults(entry.getKey(), entry.getValue());
    }

    for (final File file : myTestsXmlFiles){
      myLogger.message("XXX: " + file.getName());
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
        publicTestCaseInfo(testName, res);
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
    try {
      XMLInputFactory f = XMLInputFactory.newInstance();
      XMLStreamReader reader = f.createXMLStreamReader(new FileReader(xmlFile));
      String testResult = null;
      String testName = null;
      String tempTag = "";
      while (reader.hasNext()){
        if (reader.isStartElement() && reader.hasName() && reader.getLocalName().equalsIgnoreCase("test")){
          testResult = null;
          testName = null;
        }
        if (reader.isEndElement() && reader.hasName() && reader.getLocalName().equalsIgnoreCase("test")){
          if (testResult != null && testName != null){
            publicTestCaseInfo(testName, testResult);
          }
        }

        if (reader.hasName()){
          tempTag = reader.getLocalName();
        }

        if (reader.hasText()){
          if (tempTag.equalsIgnoreCase("result")){
            testResult = reader.getText();
          }
          else if (tempTag.equalsIgnoreCase("name")){
            testName = reader.getText();
          }
        }
        reader.next();
      }
      myLogger.message("Finished parse " + xmlFile.getName());
      reader.close();
    } catch (FileNotFoundException e) {
      Loggers.AGENT.warn("AutotoolsTestRepoter: parseXmlTestsResults exception " + e.getMessage());
    } catch (XMLStreamException e) {
      Loggers.AGENT.warn("AutotoolsTestRepoter: parseXmlTestsResults exception " + e.getMessage());
    }
  }
  /**
   * Public result of testcase on test testName
   * @param testName name of test
   * @param result result of test
   */
  private void publicTestCaseInfo(@NotNull final String testName,@NotNull final String result){
    myLogger.logTestStarted(testName);
    if (Arrays.asList(failTestResults).contains(result)){
      myLogger.logTestFailed(testName, "Failed", result);
    }
    else
      if (!Arrays.asList(successTestResults).contains(result)){
        myLogger.logTestIgnored(testName, result);
      }
    myLogger.logTestFinished(testName);
  }
}
