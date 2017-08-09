package jetbrains.buildServer.autotools.agent;

import com.google.common.annotations.GwtCompatible;
import com.intellij.execution.configurations.GeneralCommandLine;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import static jetbrains.buildServer.autotools.common.AutotoolsBuildConstants.*;

/**
 * Created on 26.07.2017.
 * Author     : Nadezhda Demina
 */
public class AutotoolsLifeCycleListener extends AgentLifeCycleAdapter {

  /**
   * Does autotools test report
   */
  private AutotoolsTestsReporter testReporter;
  AutotoolsLifeCycleListener(EventDispatcher<AgentLifeCycleListener> dispatcher) {
    dispatcher.addListener(this);
  }


  /**
   * Returns true if version v1 < version v2.
   * @param v1 first version
   * @param v2 second version
   * @return true if version v1 < version, v2 else false
   */
  @NotNull
  @GwtCompatible
  static boolean compareVersions(@NotNull final String v1,@NotNull final String v2){
    final String[] v1mas = v1.split(".");
    final String[] v2mas = v2.split(".");
    for (int i = 0; i < Math.min(v1mas.length, v2mas.length); i++){
      if (Integer.parseInt(v1mas[i]) != Integer.parseInt(v2mas[i])){
        return Integer.parseInt(v1mas[i]) < Integer.parseInt(v2mas[i]);
      }
    }
    return v1mas.length < v2mas.length;
  }

  /**
   * Find runtest tool
   * @param runner BuildRunnerContext
   */
  private void findToolRuntest(@NotNull final BuildRunnerContext runner){
    final GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath("runtest");
    commandLine.addParameter("--version");
    final ExecResult execResult = SimpleCommandLineProcessRunner.runCommand(commandLine, (byte[])null);
    if (execResult.getExitCode() != 0) {
      return;
    }
    String version = "";
    for (final String line : execResult.getOutLines()) {
      if (!line.startsWith("DejaGnu version")) continue;
      Matcher matcher = Pattern.compile("\\d+(\\.\\d+)+").matcher(line);
      if (matcher.find()) {
        version = matcher.group();
      }
    }
    runner.addRunnerParameter(HAS_RUNTEST_VAR, "1");
    runner.addRunnerParameter(RUNTEST_XML_FILE_VAR, Boolean.toString(compareVersions(version, "1.6")));
  }
  @Override
  public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
    findToolRuntest(runner);
    testReporter = new AutotoolsTestsReporter(System.currentTimeMillis(), runner.getBuild().getBuildLogger(),
                                              runner.getBuild().getCheckoutDirectory().getAbsolutePath() + "/",
                                              Boolean.parseBoolean(runner.getRunnerParameters().get(UI_NEED_DEJAGNU_VALID_XML)));
    testReporter.findDejagnu(runner.getBuild().getCheckoutDirectory());
  }



  @Override
  public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
    super.runnerFinished(runner, status);
    testReporter.searchTestsFiles(runner.getBuild().getCheckoutDirectory());
    testReporter.doTestsReport();
  }
}
