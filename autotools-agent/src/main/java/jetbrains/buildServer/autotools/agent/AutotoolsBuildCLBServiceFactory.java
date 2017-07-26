package jetbrains.buildServer.autotools.agent;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import jetbrains.buildServer.autotools.common.AutotoolsBuildConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 12.07.2017.
 * @author     : Nadezhda Demina
 */
public final class AutotoolsBuildCLBServiceFactory implements CommandLineBuildServiceFactory, AgentBuildRunnerInfo {
  @Override
  @NotNull
  public String getType() {
    return AutotoolsBuildConstants.TYPE;
  }

  @Override
  public boolean canRun(@NotNull  final BuildAgentConfiguration buildAgentConfiguration) {
    return buildAgentConfiguration.getSystemInfo().isUnix();
  }

  @Override
  @NotNull
  public CommandLineBuildService createService() {
    return new AutotoolsBuildCLBService();
  }

  @NotNull
  @Override
  public AgentBuildRunnerInfo getBuildRunnerInfo() {
    return this;
  }

}
