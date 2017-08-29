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
public final class AutotoolsBuildRunType extends RunType{

  /**
   * Descriptor for AutotoolsRunnerPlugin.
   */
  private final PluginDescriptor myPluginDescriptor;
  private AutotoolsBuildRunType(final @NotNull PluginDescriptor descriptor) {
    myPluginDescriptor = descriptor;
  }

  public static @NotNull RunType getInstance(final @NotNull RunTypeRegistry runTypeRegistry, final @NotNull PluginDescriptor descriptor){
    final RunType runType = new AutotoolsBuildRunType(descriptor);
    runTypeRegistry.registerRunType(runType);
    return runType;
  }
  @Override
  public @NotNull String getType() {
    return TYPE;
  }

  @Override
  public @NotNull String getDisplayName() {
    return DISPLAY_NAME;
  }

  @Override
  public @NotNull String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public @Nullable PropertiesProcessor getRunnerPropertiesProcessor() {
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

  @Override
  public @NotNull Map<String, String> getDefaultRunnerProperties() {

    final Map<String, String> properties = new HashMap<>();
    properties.put(UI_NEED_AUTORECONF, Boolean.toString(true));
    properties.put(UI_ADDITIONAL_MAKE_PARAMS, "all");
    properties.put(UI_ADDITIONAL_CONF_PARAMS, "");
    properties.put(UI_MAKE_CHECK, "check");
    properties.put(UI_DEJAGNU_XML_REPLACE_AMP, Boolean.toString(false));
    properties.put(UI_DEJAGNU_XML_REPLACE_CONTROLS, Boolean.toString(true));
    return properties;
  }

  @Override
  public @NotNull List<Requirement> getRunnerSpecificRequirements(final @NotNull Map<String, String> runParameters) {
    final List<Requirement> requirements = new ArrayList<>(4);
    requirements.add(new Requirement(TOOL_AUTOCONF, null, RequirementType.EXISTS));
    requirements.add(new Requirement(TOOL_MAKE, null, RequirementType.EXISTS));
    requirements.add(new Requirement(TOOL_TAR, null, RequirementType.EXISTS));
    requirements.add(new Requirement(TOOL_GZIP, null, RequirementType.EXISTS));
    return requirements;
  }
}
