package jetbrains.buildServer.autotools.server;

/**
 * Created by naduxa on 11.07.2017.
 */
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.autotools.common.AutotoolsConfigureConstants.*;

public class AutotoolsRunType extends RunType{

  private final PluginDescriptor myPluginDescriptor;
  public AutotoolsRunType(@NotNull final RunTypeRegistry runTypeRegistry, @NotNull final PluginDescriptor descriptor) {
    runTypeRegistry.registerRunType(this);
    myPluginDescriptor = descriptor;
  }
  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return null;
  }

  @Override
  public String getEditRunnerParamsJspFilePath() {
    return myPluginDescriptor.getPluginResourcesPath("editAutotoolsConfigureRunner.jsp");
  }

  @Override
  public String getViewRunnerParamsJspFilePath() {
    return myPluginDescriptor.getPluginResourcesPath("viewAutotoolsConfigureRunner.jsp");
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    final String trueStr = Boolean.toString(true);
    final Map<String, String> properties = new HashMap<>();
    properties.put(UI_REDIRECT_STDERR, trueStr);
    properties.put(UI_NEED_AUTORECONF, trueStr);
    return properties;
  }
}
