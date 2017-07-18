package jetbrains.buildServer.autotools.server;

import org.jetbrains.annotations.NotNull;
import static jetbrains.buildServer.autotools.common.AutotoolsBuildConstants.*;

/**
 * Created by naduxa on 12.07.2017.
 */

public class AutotoolsBuildBean {
  @NotNull
  public String getAdditionalConfigurateParamsKey() {
    return UI_ADDITIONAL_CONF_PARAMS;
  }

  @NotNull
  public String getAdditionalMakeParamsKey() {
    return UI_ADDITIONAL_MAKE_PARAMS;
  }

  @NotNull
  public String getRedirectStderrKey() {
    return UI_REDIRECT_STDERR;
  }

  public String getNeedAutoreconf() {
    return UI_NEED_AUTORECONF;
  }
  public String getConfigurePath(){
    return UI_SOURCE_PATH;
  }

}
