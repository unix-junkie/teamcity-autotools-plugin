package jetbrains.buildServer.autotools.agent;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import static jetbrains.buildServer.autotools.common.AutotoolsBuildConstants.*;

/**
 * @author Nadezhda Demina
 */
public final class AutotoolsLifeCycleListener extends AgentLifeCycleAdapter {
  private AutotoolsTestsReporter myTestReporter;

  private final ToolProvidersRegistry myProvidersRegistry;

  AutotoolsLifeCycleListener(final EventDispatcher<AgentLifeCycleListener> dispatcher, final ToolProvidersRegistry toolProvidersRegistry) {
    dispatcher.addListener(this);
    myProvidersRegistry = toolProvidersRegistry;
  }

  @Override
  public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
    final RuntestToolProvider runtestToolProvider = (RuntestToolProvider) myProvidersRegistry.findToolProvider(TOOL_RUNTEST);
    if (runtestToolProvider != null){
      runtestToolProvider.setDejagnuParameters(runner);
    }
    myTestReporter = new AutotoolsTestsReporter(System.currentTimeMillis(), runner.getBuild().getBuildLogger(),
                                                runner.getBuild().getCheckoutDirectory().getAbsolutePath() + '/',
                                              Boolean.parseBoolean(runner.getRunnerParameters().get(UI_DEJAGNU_XML_REPLACE_AMP)),
                                                Boolean.parseBoolean(runner.getRunnerParameters().get(UI_DEJAGNU_XML_REPLACE_CONTROLS)));

    myTestReporter.findDejagnu(runner.getBuild().getCheckoutDirectory());
  }



  @Override
  public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
    super.runnerFinished(runner, status);
    myTestReporter.searchTestsFiles(runner.getBuild().getCheckoutDirectory());
    myTestReporter.doTestsReport();
  }
}
