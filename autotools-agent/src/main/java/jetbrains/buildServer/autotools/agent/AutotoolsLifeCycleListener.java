package jetbrains.buildServer.autotools.agent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.util.text.VersionComparatorUtil;
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
  RuntestToolProvider myRuntestToolProvider;
  private AutotoolsTestsReporter myTestReporter;
  private ToolProvidersRegistry myProvidersRegistry;
  AutotoolsLifeCycleListener(EventDispatcher<AgentLifeCycleListener> dispatcher, ToolProvidersRegistry toolProvidersRegistry) {
    dispatcher.addListener(this);
    myProvidersRegistry = toolProvidersRegistry;
  }

  @Override
  public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
    myRuntestToolProvider = (RuntestToolProvider) myProvidersRegistry.findToolProvider(TOOL_RUNTEST);
    if (myRuntestToolProvider != null){
      myRuntestToolProvider.setDejagnuParameters(runner);
    }
    myTestReporter = new AutotoolsTestsReporter(System.currentTimeMillis(), runner.getBuild().getBuildLogger(),
                                              runner.getBuild().getCheckoutDirectory().getAbsolutePath() + "/",
                                              Boolean.parseBoolean(runner.getRunnerParameters().get(UI_NEED_DEJAGNU_VALID_XML)));

    myTestReporter.findDejagnu(runner.getBuild().getCheckoutDirectory());
  }



  @Override
  public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
    super.runnerFinished(runner, status);
    myTestReporter.searchTestsFiles(runner.getBuild().getCheckoutDirectory());
    myTestReporter.doTestsReport();
  }
}
