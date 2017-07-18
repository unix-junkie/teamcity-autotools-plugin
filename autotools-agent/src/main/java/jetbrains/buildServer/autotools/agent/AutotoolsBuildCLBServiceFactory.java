package jetbrains.buildServer.autotools.agent;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jetbrains.buildServer.autotools.common.AutotoolsBuildConstants;
import jetbrains.buildServer.log.Loggers;

/**
 * Created by naduxa on 12.07.2017.
 */
public class AutotoolsBuildCLBServiceFactory implements CommandLineBuildServiceFactory, AgentBuildRunnerInfo {
  public AutotoolsBuildCLBServiceFactory() {
    Loggers.AGENT.info("XXX: Downloading AutotoolsBuildCLBServiceFactory");
  }
  @Override
  public String getType() {
    return AutotoolsBuildConstants.TYPE;
  }

  @Override
  public boolean canRun(final BuildAgentConfiguration buildAgentConfiguration) {
    return buildAgentConfiguration.getSystemInfo().isUnix();
  }

  @Override
  public CommandLineBuildService createService() {
    return new AutotoolsBuildCLBService();
  }

  @Override
  public AgentBuildRunnerInfo getBuildRunnerInfo() {
    return this;
  }

}
