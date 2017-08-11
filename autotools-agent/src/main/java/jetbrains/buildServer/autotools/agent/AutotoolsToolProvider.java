package jetbrains.buildServer.autotools.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 10.08.2017.
 * Author     : Nadezhda Demina
 */
public class AutotoolsToolProvider extends AgentLifeCycleAdapter implements ToolProvider {
  /**
   * Tool Name
   */
  private final String myToolName;
  public AutotoolsToolProvider(ToolProvidersRegistry toolProvidersRegistry,
                               EventDispatcher<AgentLifeCycleListener> eventDispatcher, String toolName){
    toolProvidersRegistry.registerToolProvider(this);
    eventDispatcher.addListener(this);
    myToolName = toolName;
  }


  @Override
  public void beforeAgentConfigurationLoaded(@NotNull BuildAgent agent) {
    if (isExistedTool()){
      Loggers.AGENT.info("AutotoolsToolProvider for tool " + myToolName + " found");
      agent.getConfiguration().addConfigurationParameter(myToolName, myToolName);
      return;
    }

    Loggers.AGENT.info("AutotoolsToolProvider for tool " + myToolName + "did not find");
  }


  /**
   * Returns True if Existed Tool myConfigName
   * @return
   */
  public boolean isExistedTool(){
    final GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath(myToolName);
    commandLine.addParameter("--version");
    final ExecResult execResult = SimpleCommandLineProcessRunner.runCommand(commandLine, (byte[])null);
    return execResult.getExitCode() == 0;
  }

  @NotNull
  @Override
  public boolean supports(@NotNull final String s) {
    return myToolName.equals(s);
  }

  @NotNull
  @Override
  public String getPath(@NotNull final String s) throws ToolCannotBeFoundException {
    return myToolName;
  }

  @NotNull
  @Override
  public String getPath(@NotNull final String s, @NotNull final AgentRunningBuild agentRunningBuild, @NotNull final BuildRunnerContext buildRunnerContext) throws ToolCannotBeFoundException {
    return myToolName;
  }
}
