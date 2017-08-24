package jetbrains.buildServer.autotools.agent;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.execution.configurations.GeneralCommandLine;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
@SuppressWarnings("ALL")
public class AutotoolsToolProvider extends AgentLifeCycleAdapter implements ToolProvider {
  /**
   * Tool Name
   */

  protected static final String regVersionNumer = "\\d+(?:\\.\\d+)+";
  protected String myVersion;
  protected final String myToolName;
  protected final String myVersionArg;

  @VisibleForTesting
  public AutotoolsToolProvider(@NotNull final String toolName, @NotNull final String versionArg){
    myToolName = toolName;
    myVersionArg = versionArg;
  }

  public AutotoolsToolProvider(@NotNull final ToolProvidersRegistry toolProvidersRegistry,
                               @NotNull final EventDispatcher<AgentLifeCycleListener> eventDispatcher, @NotNull final String toolName, @NotNull final String versionArg){
    toolProvidersRegistry.registerToolProvider(this);
    eventDispatcher.addListener(this);
    myVersionArg = versionArg;
    myToolName = toolName;
  }


  @Override
  public void beforeAgentConfigurationLoaded(@NotNull final BuildAgent agent) {
    if (isExistedTool()){
      Loggers.AGENT.info("AutotoolsToolProvider for tool " + myToolName + " found");
      agent.getConfiguration().addConfigurationParameter(myToolName, myToolName);
      return;
    }

    Loggers.AGENT.info("AutotoolsToolProvider for tool " + myToolName + "did not find");
  }

  /**
   * Returns string from string array with splitter '\n'
   * @param strArr array of strings
   * @return string joined of strArr
   */
  @NotNull
  @VisibleForTesting
  static String stringArrayToString(final String[] strArr){
    if (strArr.length == 0){
      return "";
    }
    final StringBuilder result = new StringBuilder();
    result.append(strArr[0]);
    for (int i = 1; i < strArr.length; i++){
      result.append('\n');
      result.append(strArr[i]);
    }
    return result.toString();
  }
  /**
   * Returns True if Existed Tool myConfigName
   * @return
   */
  public boolean isExistedTool(){
    final GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath(myToolName);
    commandLine.addParameter(myVersionArg);
    final ExecResult execResult = SimpleCommandLineProcessRunner.runCommand(commandLine, (byte[])null);
    if (execResult.getExitCode() == 0){
      myVersion = findVersion(stringArrayToString(execResult.getOutLines()));
    }
    return execResult.getExitCode() == 0;
  }

  @NotNull
  @VisibleForTesting
  String findVersion(@NotNull final String text) {
    final Pattern p = Pattern.compile(regVersionNumer);
    final Matcher matcher = p.matcher(text);
    return matcher.find() ? text.substring(matcher.start(), matcher.end()) : "";
  }
  @NotNull
  public String getVersion(){
    return myVersion;
  }

  @NotNull
  @Override
  public boolean supports(@NotNull final String s) {
    return myToolName.equals(s) && isExistedTool();
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
