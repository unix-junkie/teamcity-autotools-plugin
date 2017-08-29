package jetbrains.buildServer.autotools.agent;

import com.google.common.annotations.VisibleForTesting;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.log.Loggers;
import org.aspectj.weaver.ast.Test;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Nadezhda Demina
 */


final class AutotoolsTestsReporter {
  /**
   * Test Results Enum
   */
  enum TestStatus{
    XPASS, FAIL, ERROR, UNRESOLVED,
    PASS, XFAIL,
    SKIP, UNTESTED, UNSUPPORTED, WARNING, NOTE;

    @Nullable
    public static TestStatus safeValueOf(final String name){
      try{
        return valueOf(name);
      }
      catch (final IllegalArgumentException e){
        return null;
      }
    }
  }

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
   * Constant enumSet with values of success TestResults.
   */

  private static final EnumSet<TestStatus> SUCCESS_TEST_RESULTS_SET = EnumSet.of(TestStatus.PASS, TestStatus.XFAIL);


  /**
   * Constant enumSet with values of fail TestResults.
   */
  private static final EnumSet<TestStatus> FAIL_TEST_RESULTS_SET = EnumSet.of(TestStatus.ERROR, TestStatus.FAIL, TestStatus.UNRESOLVED, TestStatus.XPASS);
  //private static final String FAIL_TEST_RESULTS[] = {"XPASS", "FAIL", "ERROR", "UNRESOLVED"};
  /**
   * Constant enumSet with values of skip TestResults.
   */
  private static final EnumSet<TestStatus> SKIP_TEST_RESULTS_SET = EnumSet.of(TestStatus.SKIP, TestStatus.WARNING, TestStatus.NOTE, TestStatus.UNSUPPORTED, TestStatus.UNTESTED);

  private static final  Pattern TRS_PATTERN = Pattern.compile("\\.trs$");
  private static final Pattern LOG_PATTERN = Pattern.compile("\\.log$");
  private static final Pattern TEST_RESULT_PATTERN = Pattern.compile("\\:test\\-result\\:\\s*(PASS|FAIL|SKIP|ERROR)");
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


  private AutotoolsTestsReporter(final long timeBeforeStart, @NotNull final BuildProgressLogger logger,
                                @NotNull final String srcPath) {
    myTimeBeforeStart = timeBeforeStart;
    myLogger = logger;
    mySrcPath = srcPath;
    myTestsLogFiles = new HashMap<String, File>();
    myTestsTrsFiles = new HashMap<String, File>();
    myTestsXmlFiles = new ArrayList<File>();
    hasDejagnu = false;

  }

  public static AutotoolsTestsReporter getInstance(final long timeBeforeStart, @NotNull final BuildProgressLogger logger,
                                                   @NotNull final String srcPath, @NotNull final Boolean needReplaceAmp,
                                                   @NotNull final Boolean needReplaceControls){
    final  AutotoolsTestsReporter autotoolsTestsReporter = new AutotoolsTestsReporter(timeBeforeStart, logger, srcPath);
    autotoolsTestsReporter.myXmlParser = new DejagnuTestsXMLParser(autotoolsTestsReporter, needReplaceAmp, needReplaceControls);
    return autotoolsTestsReporter;
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
    myLogger.message("I was this!");
    if (srcDir.listFiles() == null) return;
    for (final File file : srcDir.listFiles()){
      if (file.isDirectory()){
        searchTestsFiles(file);
      }
      if (isThisExtensionFile(file, ".trs")){
        final String testName = TRS_PATTERN.matcher(getRelativePath(file.getAbsolutePath())).replaceFirst("");
        if (file.lastModified() >= myTimeBeforeStart) {
          myTestsTrsFiles.put(testName, file);
        }
        else{
          myLogger.warning("Make check: Test result file " + file.getPath() + " was modifed before build step.");
        }
      }

      if (isThisExtensionFile(file, ".log")) {
        final String testName = LOG_PATTERN.matcher(getRelativePath(file.getAbsolutePath())).replaceFirst("");
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
      while ((str = bufferead.readLine()) != null) {
        final Matcher matcher = TEST_RESULT_PATTERN.matcher(str);
        if (matcher.find()) {
          final String res = matcher.group(1);
          publicTestCaseInfo(testName, res, "");
          if (FAIL_TEST_RESULTS_SET.contains(TestStatus.safeValueOf(res))) {
            testFailed = true;
          }
        }
      }

      if (testFailed && myTestsLogFiles.containsKey(testName)) {
        myLogger.message("##teamcity[publishArtifacts '" + myTestsLogFiles.get(testName) + "']");
      }
    }
    catch (final Exception e){
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
    if (FAIL_TEST_RESULTS_SET.contains(TestStatus.safeValueOf(result))){
      myLogger.logTestFailed(testName, "Failed", result);
    }
    else
      if (SUCCESS_TEST_RESULTS_SET.contains(TestStatus.safeValueOf(result))){
        myLogger.logTestIgnored(testName, result);
      }
      else{
        if (!SKIP_TEST_RESULTS_SET.contains(TestStatus.safeValueOf(result))){
          myLogger.warning("Unknown test result " + result + " of test " + testName);
        }
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
