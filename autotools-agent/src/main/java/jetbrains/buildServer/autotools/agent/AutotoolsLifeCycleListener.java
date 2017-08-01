package jetbrains.buildServer.autotools.agent;

import java.io.*;
import java.util.*;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 26.07.2017.
 * Author     : Nadezhda Demina
 */
public class AutotoolsLifeCycleListener extends AgentLifeCycleAdapter {

  /**
   * Current buildLogger
   */
  private  BuildProgressLogger myLogger;
  /**
   * Time of current build runner start
   */
  private  long myTimeBeforeStart;
  /**
   * .log files with test results
   */
  private Map<String, File> myTestsLogFiles;
  /**
   * .trs files with test results
   */
  private Map<String, File> myTestsTrsFiles;
  /**
   * Path of checkout directory
   */
  private String srcPath = "";

  /**
   * Constant array with values of fail TestResults
   */
  private final static String failTestResults[] = {"XPASS", "FAIL", "ERROR"};
  /**
   * Constant value of skip test
   */
  private final static String skipTestResult = "SKIP";

  public AutotoolsLifeCycleListener(EventDispatcher<AgentLifeCycleListener> dispatcher) {
    dispatcher.addListener(this);
  }
  @Override
  public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
    myTimeBeforeStart = System.currentTimeMillis();
    myTestsLogFiles = new HashMap<String, File>();
    myTestsTrsFiles = new HashMap<String, File>();

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
  private void searchTestsFiles(@NotNull final File srcDir){
    for (final File file : srcDir.listFiles()){
      if (file.isDirectory()){
        searchTestsFiles(file);
        //continue;
      }
      if (isThisExtensionFile(file, ".trs")){
        final String testName = file.getAbsolutePath().replace(".trs", "").replaceFirst(srcPath, "");;
        if (file.lastModified() >= myTimeBeforeStart) {
          myTestsTrsFiles.put(testName, file);
        }
        else{
          myLogger.warning("Make check: Test result file " + file.getPath() + " was modifed before build step.");
        }
      }
    }

    for (final File file : srcDir.listFiles()) {
      if (file.isDirectory()) {
        searchTestsFiles(file);
        //continue;
      }

      if (isThisExtensionFile(file, ".log")) {
        final String testName = file.getAbsolutePath().replace(".log", "").replaceFirst(srcPath, "");
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

  @Override
  public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
    super.runnerFinished(runner, status);
    srcPath = runner.getBuild().getCheckoutDirectory().getAbsolutePath() + "/";
    myLogger = runner.getBuild().getBuildLogger();
    searchTestsFiles(runner.getBuild().getCheckoutDirectory());
    doTestsReport();
  }

  /**
   * Handles all tests results files.
   */
  private void doTestsReport(){
    for (final Map.Entry<String, File> entry : myTestsTrsFiles.entrySet()){
      publicTestResults(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Parse results test testName what is readed from trsFile.
   * @param testName name of test
   * @param trsFile test result file
   * @throws IOException
   */
  private void  publicTestResults(@NotNull final String testName, @NotNull final File trsFile){
    try {
      final BufferedReader bufferead = new BufferedReader(new FileReader(trsFile));
      String str = "";
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
   * Public result of testcase on test testName
   * @param testName name of test
   * @param result result of test
   */
  private void publicTestCaseInfo(@NotNull final String testName,@NotNull final String result){
    myLogger.logTestStarted(testName);
    if (Arrays.asList(failTestResults).contains(result)){
      myLogger.logTestFailed(testName, "Failed", result);
    }
    if (result.equalsIgnoreCase(skipTestResult)){
      myLogger.logTestIgnored(testName, result);
    }
    myLogger.logTestFinished(testName);
  }
}
