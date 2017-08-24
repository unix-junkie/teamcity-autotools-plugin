package jetbrains.buildServer.autotools.server;

import org.jetbrains.annotations.NotNull;
import static jetbrains.buildServer.autotools.common.AutotoolsBuildConstants.*;

/**
 * Created on 12.07.2017.
 * @author     : Nadezhda Demina
 */
public final class AutotoolsBuildBean {

  /**
   * Returns parameter name of ./configure parameters and flags.
   * @return parameter name of ./configure parameters
   */
  @SuppressWarnings({"MethodMayBeStatic", "SameReturnValue"})
  public @NotNull String getAdditionalConfigurateParamsKey() {
    return UI_ADDITIONAL_CONF_PARAMS;
  }

  /**
   * Returns parameter name of make parameters and flags.
   * @return parameter name of make parameters
   */
  @SuppressWarnings({"MethodMayBeStatic", "SameReturnValue"})
  public @NotNull String getAdditionalMakeParamsKey() {
    return UI_ADDITIONAL_MAKE_PARAMS;
  }

  /**
   * Return parameter name of flag needed autoreconf.
   * @return parameter name of flag needed autoreconf
   */
  @SuppressWarnings({"MethodMayBeStatic", "SameReturnValue"})
  public @NotNull String getNeedAutoreconf() {
    return UI_NEED_AUTORECONF;
  }

  /**
   * Return parameter name of flag needed dejagnu xml replaces &.
   * @return parameter name of flag needed dejagnu xml replaces &
   */
  @SuppressWarnings({"MethodMayBeStatic", "SameReturnValue"})
  public @NotNull String getNeedDejagnuXmlReplaceAmp(){
    return UI_DEJAGNU_XML_REPLACE_AMP;
  }


  /**
   * Return parameter name of flag needed dejagnu xml replaces &.
   * @return parameter name of flag needed dejagnu xml replaces &
   */
  @SuppressWarnings({"MethodMayBeStatic", "SameReturnValue"})
  public @NotNull String getNeedDejagnuXmlReplaceControls(){
    return UI_DEJAGNU_XML_REPLACE_CONTROLS;
  }

  /**
   * Returns parameter name of path to source directory.
   * @return
   */
  @SuppressWarnings({"MethodMayBeStatic", "SameReturnValue"})
  public @NotNull String getSourcePath(){
    return UI_SOURCE_PATH;
  }

  /**
   * Returns parameter name of make check parameters and flags.
   * @return
   */
  @SuppressWarnings({"MethodMayBeStatic", "SameReturnValue"})
  public @NotNull String getMakeCheckParam(){
    return UI_MAKE_CHECK;
  }
}
