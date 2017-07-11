package jetbrains.buildServer.autotools.server;

/**
 * Created by naduxa on 11.07.2017.
 */
import java.util.Map;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import static jetbrains.buildServer.autotools.common.AutotoolsConfigureConstants.*;

public class AutotoolsConfigureRunType extends RunType{
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
    return null;
  }

  @Override
  public String getViewRunnerParamsJspFilePath() {
    return null;
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return null;
  }
}
