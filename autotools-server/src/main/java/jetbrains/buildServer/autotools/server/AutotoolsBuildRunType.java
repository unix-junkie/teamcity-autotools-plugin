package jetbrains.buildServer.autotools.server;

import java.util.*;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.requirements.RequirementType;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.autotools.common.AutotoolsBuildConstants.*;

/**
 * Created on 12.07.2017.
 * @author     : Nadezhda Demina
 */
public class AutotoolsBuildRunType extends RunType{

  /**
   * Descriptor for AutotoolsRunnerPlugin
   */
  private final PluginDescriptor myPluginDescriptor;
  public AutotoolsBuildRunType(@NotNull final RunTypeRegistry runTypeRegistry, @NotNull final PluginDescriptor descriptor) {
    runTypeRegistry.registerRunType(this);
    myPluginDescriptor = descriptor;
  }

  @Override
  @NotNull
  public String getType() {
    return TYPE;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  @NotNull
  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Nullable
  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return null;
  }


  @Override
  public String getEditRunnerParamsJspFilePath() {
    return myPluginDescriptor.getPluginResourcesPath("editAutotoolsBuildRunner.jsp");
  }

  @Override
  public String getViewRunnerParamsJspFilePath() {
    return myPluginDescriptor.getPluginResourcesPath("viewAutotoolsBuildRunner.jsp");
  }

  @NotNull
  @Override
  public Map<String, String> getDefaultRunnerProperties() {

    final Map<String, String> properties = new HashMap<>();
    properties.put(UI_NEED_AUTORECONF, Boolean.toString(true));
    properties.put(UI_ADDITIONAL_MAKE_PARAMS, "all");
    properties.put(UI_ADDITIONAL_CONF_PARAMS, "");
    properties.put(UI_MAKE_CHECK, "check");
    properties.put(UI_NEED_DEJAGNU_VALID_XML, Boolean.toString(true));
    return properties;
  }

  @NotNull
  @Override
  public List<Requirement> getRunnerSpecificRequirements(@NotNull Map<String, String> runParameters) {
    final List<Requirement> requirements = new LinkedList<>();
    requirements.add(new Requirement(TOOL_AUTOCONF, null, RequirementType.EXISTS));
    requirements.add(new Requirement(TOOL_MAKE, null, RequirementType.EXISTS));
    requirements.add(new Requirement(TOOL_TAR, null, RequirementType.EXISTS));
    requirements.add(new Requirement(TOOL_GZIP, null, RequirementType.EXISTS));
    return requirements;
  }
}
