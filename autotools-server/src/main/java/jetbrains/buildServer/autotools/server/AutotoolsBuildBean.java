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
  @NotNull
  public String getAdditionalConfigurateParamsKey() {
    return UI_ADDITIONAL_CONF_PARAMS;
  }

  /**
   * Returns parameter name of make parameters and flags.
   * @return parameter name of make parameters
   */
  @NotNull
  public String getAdditionalMakeParamsKey() {
    return UI_ADDITIONAL_MAKE_PARAMS;
  }

  /**
   * Return parameter name of flag needed autoreconf.
   * @return parameter name of flag needed autoreconf
   */
  @NotNull
  public String getNeedAutoreconf() {
    return UI_NEED_AUTORECONF;
  }

  /**
   * Return parameter name of flag needed dejagnu xml replaces &
   * @return parameter name of flag needed dejagnu xml replaces &
   */
  @NotNull
  public String getNeedDejagnuXmlReplaceAmp(){
    return UI_DEJAGNU_XML_REPLACE_AMP;
  }


  /**
   * Return parameter name of flag needed dejagnu xml replaces &
   * @return parameter name of flag needed dejagnu xml replaces &
   */
  @NotNull
  public String getNeedDejagnuXmlReplaceControls(){
    return UI_DEJAGNU_XML_REPLACE_CONTROLS;
  }

  /**
   * Returns parameter name of path to source directory
   * @return
   */
  @NotNull
  public String getSourcePath(){
    return UI_SOURCE_PATH;
  }

  /**
   * Returns parameter name of make check parameters and flags
   * @return
   */
  @NotNull
  public String getMakeCheckParam(){
    return UI_MAKE_CHECK;
  }
}
