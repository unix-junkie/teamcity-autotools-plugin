package jetbrains.buildServer.autotools.agent;

import com.google.common.annotations.VisibleForTesting;
import java.io.*;
import java.util.*;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.log.Loggers;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 02.08.2017.
 * Author     : Nadezhda Demina
 */
public final class AutotoolsTestsReporter {
  /**
   * Current buildLogger.
   */
  private final BuildProgressLogger myLogger;
  /**
   * Time of current build runner start.
   */
  private final long myTimeBeforeStart;
  /**
   * .log files with test results.
   */
  private final Map<String, File> myTestsLogFiles;
  /**
   * .trs files with test results.
   */
  private final Map<String, File> myTestsTrsFiles;

  /**
   * .xml files with test results.
   */
  private final List<File> myTestsXmlFiles;
  /**
   * Path of checkout directory.
   */

  private final String mySrcPath;
  /**
   * Flag to know has Dejagnu test Framework.
   */
  private Boolean hasDejagnu;

  /**
   * Dejagnu Tests Xml Parser
   */
  private DejagnuTestsXMLParser myXmlParser;
  /**
   * Constant array with values of success TestResults.
   */

  private final static String successTestResults[] = {"PASS", "XFAIL"};

  /**
   * Constant array with values of fail TestResults.
   */
  private final static String failTestResults[] = {"XPASS", "FAIL", "ERROR", "UNRESOLVED"};
  /**
   * Constant array with values of skip TestResults.
   */
  private final static String skipTestResults[] = {"SKIP", "UNTESTED", "UNSUPPORTED", "WARNING", "NOTE"};

  @VisibleForTesting
  AutotoolsTestsReporter(@NotNull final String srcPath) {
    myTimeBeforeStart = 0;
    myLogger = null;
    mySrcPath = srcPath;
    myTestsLogFiles = new HashMap<String, File>();
    myTestsTrsFiles = new HashMap<String, File>();
    myTestsXmlFiles = new ArrayList<File>();
    hasDejagnu = false;
  }


  public AutotoolsTestsReporter(@NotNull final long timeBeforeStart, @NotNull final BuildProgressLogger logger,
                                @NotNull final String srcPath, @NotNull final Boolean needReplaceAmp,
                                @NotNull final Boolean needReplaceControls) {
    myTimeBeforeStart = timeBeforeStart;
    myLogger = logger;
    mySrcPath = srcPath;
    myTestsLogFiles = new HashMap<String, File>();
    myTestsTrsFiles = new HashMap<String, File>();
    myTestsXmlFiles = new ArrayList<File>();
    hasDejagnu = false;
    myXmlParser = new DejagnuTestsXMLParser(this, needReplaceAmp, needReplaceControls);
  }

  /**
   * Returns True if project has Dejagnu options
   *
   * @param srcDir Source directory
   * @return true if project has Dejagnu options
   */
  @NotNull
  private boolean hasDejagnuOpt(@NotNull final File srcDir) {
    try {
      final String entireFileText = new Scanner(srcDir).useDelimiter("\\A").next();
      return entireFileText.contains("dejagnu") || entireFileText.contains("DEJATOOL") || entireFileText.contains("DEJAGNU");
    } catch (IOException e) {
      return false;
    }
  }


  /**
   *
   * @param srcDir
   */
  void findDejagnu(@NotNull final File srcDir){
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
  @NotNull
  @VisibleForTesting
  static boolean isThisExtensionFile(@NotNull final File file, @NotNull final String extension){
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
  @VisibleForTesting
  String getRelativePath(@NotNull final String path){
    return path.replaceFirst(mySrcPath, "");
  }
  void searchTestsFiles(@NotNull final File srcDir){
    myLogger.message("XXX: " + srcDir.getName());
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
        final String testName = getRelativePath(file.getAbsolutePath()).replace(".log", "");
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
  void doTestsReport(){
    for (final Map.Entry<String, File> entry : myTestsTrsFiles.entrySet()){
      parseTrsTestResults(entry.getKey(), entry.getValue());
    }
    for (final File file : myTestsXmlFiles){
      myXmlParser.handleXmlResults(file);
    }
  }


  /**
   * Parse results test testName what is readed from trsFile.
   * @param testName name of test
   * @param trsFile test result file
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


  /**
   * Public result of testcase on test testName
   * @param testName name of test
   * @param result result of test
   */
  void publicTestCaseInfo(@NotNull final String testName,@NotNull final String result, @NotNull final String stdOut){
    if (myLogger == null) return;
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

  void publicTestSuiteStarted(@NotNull final String testSuiteName){
    myLogger.logSuiteStarted(getRelativePath(testSuiteName));
  }

  void publicTestSuiteFinished(@NotNull final String testSuiteName){
    myLogger.logSuiteFinished(getRelativePath(testSuiteName));
  }
  void reportWarning(@NotNull final String warn){
    myLogger.warning(warn);
  }
}
