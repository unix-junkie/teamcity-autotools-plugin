package jetbrains.buildServer.autotools.agent;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.ToolProvidersRegistry;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.autotools.common.AutotoolsBuildConstants.*;

/**
 * @author Nadezhda Demina
 */
public class RuntestToolProvider extends AutotoolsToolProvider {


  @VisibleForTesting
  RuntestToolProvider(@NotNull final String toolName, @NotNull final String versionArg){
    super(toolName, versionArg);
  }
  public RuntestToolProvider(@NotNull final ToolProvidersRegistry toolProvidersRegistry, @NotNull final EventDispatcher<AgentLifeCycleListener> eventDispatcher, @NotNull final String toolName, @NotNull final String versionArg) {
    super(toolProvidersRegistry, eventDispatcher, toolName, versionArg);
  }

  @NotNull
  @VisibleForTesting
  @Override
  String findVersion(@NotNull final String text) {
    final Pattern needVersion = Pattern.compile("(DejaGnu|Framework).+version.+" + regVersionNumer);
    Matcher matcher = needVersion.matcher(text);
    if (!matcher.find()){
      return "";
    }
    final String versionLine = text.substring(matcher.start(), matcher.end());
    final Pattern versionNumberOnly = Pattern.compile(regVersionNumer);
    matcher = versionNumberOnly.matcher(versionLine);
    return matcher.find() ? versionLine.substring(matcher.start(), matcher.end()) : "";
  }

  /**
   * Returns need runner parameters dependence current version runtest.
   * @param version current version runtest
   * @return list of need parameters
   */
  @NotNull
  @VisibleForTesting
  static List<Pair<String, String>> getDejagnuParameters(@NotNull final String version){
    final List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
    params.add(new Pair<String, String>(HAS_RUNTEST_VAR, "1"));
    if (VersionComparatorUtil.compare(version, "1.4.4") <= 0){
      params.add(new Pair<String, String>(MY_RUNTESTFLAGS, "RUNTESTFLAGS=--all"));
      return params;
    }
    if (VersionComparatorUtil.compare(version, "1.5.3") < 0){
        params.add(new Pair<String, String>(MY_RUNTESTFLAGS, "RUNTESTFLAGS=--all --xml"));
      return params;
    }
    params.add(new Pair<String, String>(MY_RUNTESTFLAGS, "RUNTESTFLAGS=--all --xml=\"testresults.xml\""));
    return params;
  }

  /**
   * set need for Dejagnu testing framework parameters.
   * @param runner
   */
  void setDejagnuParameters(@NotNull final BuildRunnerContext runner){
    if (!isExistedTool()){
      return;
    }
    final List<Pair<String, String>> params = getDejagnuParameters(myVersion);
    for(final Pair<String, String> parameter : params){
      runner.getBuild().addSharedEnvironmentVariable(parameter.first, parameter.second);
    }
  }
}
