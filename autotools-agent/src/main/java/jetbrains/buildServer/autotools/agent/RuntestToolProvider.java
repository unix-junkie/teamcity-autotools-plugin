package jetbrains.buildServer.autotools.agent;

import com.google.common.annotations.VisibleForTesting;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.ToolProvidersRegistry;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 14.08.2017.
 * Author     : Nadezhda Demina
 */
public class RuntestToolProvider extends AutotoolsToolProvider {

  public RuntestToolProvider(@NotNull final ToolProvidersRegistry toolProvidersRegistry, @NotNull final EventDispatcher<AgentLifeCycleListener> eventDispatcher, @NotNull final String toolName, @NotNull final String versionArg) {
    super(toolProvidersRegistry, eventDispatcher, toolName, versionArg);
  }

  @NotNull
  @VisibleForTesting
  @Override
  String findVersion(@NotNull final String text) {
    final Pattern needVersion = Pattern.compile("(Dejagnu|Framework).+version.+" + regVersionNumer);
    Matcher matcher = needVersion.matcher(text);
    if (!matcher.find()){
      return "";
    }
    final String versionLine = text.substring(matcher.start(), matcher.end());
    final Pattern versionNumberOnly = Pattern.compile(regVersionNumer);
    matcher = versionNumberOnly.matcher(versionLine);
    return matcher.find() ? versionLine.substring(matcher.start(), matcher.end()) : "";
  }
}
