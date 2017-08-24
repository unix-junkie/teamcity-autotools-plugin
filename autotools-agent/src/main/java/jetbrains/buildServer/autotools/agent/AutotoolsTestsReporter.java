package jetbrains.buildServer.autotools.agent;

import com.google.common.annotations.VisibleForTesting;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.log.Loggers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Nadezhda Demina
 */
final class AutotoolsTestsReporter {
  /**
   * Current buildLogger.
   */
  @Nullable
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
   * Dejagnu Tests Xml Parser.
   */
  private DejagnuTestsXMLParser myXmlParser;
  /**
   * Constant array with values of success TestResults.
   */

  private static final String SUCCESS_TEST_RESULTS[] = {"PASS", "XFAIL"};

  /**
   * Constant array with values of fail TestResults.
   */
  private static final String FAIL_TEST_RESULTS[] = {"XPASS", "FAIL", "ERROR", "UNRESOLVED"};
  /**
   * Constant array with values of skip TestResults.
   */
  private static final String SKIP_TEST_RESULTS[] = {"SKIP", "UNTESTED", "UNSUPPORTED", "WARNING", "NOTE"};

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


  AutotoolsTestsReporter(final long timeBeforeStart, @NotNull final BuildProgressLogger logger,
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
   * Returns True if project has Dejagnu options.
   *
   * @param srcDir Source directory
   * @return true if project has Dejagnu options
   */
  private static boolean hasDejagnuOpt(@NotNull final File srcDir) {
    try {
      final String entireFileText = new Scanner(srcDir).useDelimiter("\\A").next();
      return entireFileText.contains("dejagnu") || entireFileText.contains("DEJATOOL") || entireFileText.contains("DEJAGNU");
    } catch (final IOException ignored) {
      return false;
    }
  }


  /**
   * @param srcDir
   */
  void findDejagnu(@NotNull final File srcDir){
    if (hasDejagnu){
      return;
    }
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
  @VisibleForTesting
  static boolean isThisExtensionFile(@NotNull final File file, @NotNull final String extension){
    final String fileName = file.getName();
    final int dotIdx = fileName.lastIndexOf('.');
    if (dotIdx == -1){
      return false;
    }
    final String fileExtension = fileName.substring(dotIdx);
    return fileExtension.equalsIgnoreCase(extension);
  }


  @NotNull
  @VisibleForTesting
  String getRelativePath(@NotNull final String path){
    return path.replaceFirst(mySrcPath, "");
  }

  /**
   * Finds files, created after execution test, and contains test-results and put their in maps.
   * @param srcDir Directory, where will search files
   */
  void searchTestsFiles(@NotNull final File srcDir){
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
    for (final Entry<String, File> entry : myTestsTrsFiles.entrySet()){
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
    final BufferedReader bufferead;
    try {
      bufferead = new BufferedReader(new FileReader(trsFile));
    }
    catch (final IOException e){
      Loggers.AGENT.warn("AutotoolsLifeCycleListener: parseTrsTestResults:" + e.getMessage());
      return;
    }
    try {
      String str;
      boolean testFailed = false;

      // Parse each string in result file
      //noinspection NestedAssignment
      while ((str = bufferead.readLine()) != null) {
        int containsIdx = str.indexOf(":test-result:");
        if (containsIdx == -1) {
          continue;
        }
        containsIdx += ":test-result:".length() + 1;
        final String res = containsIdx + 5 <= str.length() && Character.isLetter(str.charAt(containsIdx + 4)) ? str.substring(containsIdx, containsIdx + 5) : str.substring(containsIdx, containsIdx + 4);
        publicTestCaseInfo(testName, res, "");
        if (Arrays.asList(FAIL_TEST_RESULTS).contains(res)) {
          testFailed = true;
        }
      }

      if (testFailed && myTestsLogFiles.containsKey(testName)) {
        myLogger.message("##teamcity[publishArtifacts '" + myTestsLogFiles.get(testName) + "']");
      }
    }
    catch (final IOException e){
      Loggers.AGENT.warn("AutotoolsLifeCycleListener: In doTestReport method can't read test result file " + trsFile.getAbsolutePath() + ". " + e.getMessage());
    }
    finally {
      try {
        bufferead.close();
      } catch (final IOException e) {
        Loggers.AGENT.warn("AutotoolsLifeCycleListener: parseTrsTestResults:" + e.getMessage());
      }
    }
  }

  /**
   * Public result of testcase on test testName.
   * @param testName name of test
   * @param result result of test
   */
  void publicTestCaseInfo(@NotNull final String testName,@NotNull final String result, @NotNull final String stdOut){
    myLogger.logTestStarted(testName);
    if (Arrays.asList(FAIL_TEST_RESULTS).contains(result)){
      myLogger.logTestFailed(testName, "Failed", result);
    }
    else
      if (!Arrays.asList(SUCCESS_TEST_RESULTS).contains(result)){
        myLogger.logTestIgnored(testName, result);
      }
    if (!stdOut.isEmpty()){
        myLogger.logTestStdOut(testName, stdOut);
    }
    myLogger.logTestFinished(testName);
  }

  /**
   * Publicates test suite start.
   * @param testSuiteName name of test suite
   */
  void publicTestSuiteStarted(@NotNull final String testSuiteName){
    myLogger.logSuiteStarted(getRelativePath(testSuiteName));
  }

  /**
   * Publicates test suite finish.
   * @param testSuiteName name of test suite
   */
  void publicTestSuiteFinished(@NotNull final String testSuiteName){
    myLogger.logSuiteFinished(getRelativePath(testSuiteName));
  }

  /**
   * Report warning.
   * @param warn warning message
   */
  void reportWarning(@NotNull final String warn){
    myLogger.warning(warn);
  }
}
