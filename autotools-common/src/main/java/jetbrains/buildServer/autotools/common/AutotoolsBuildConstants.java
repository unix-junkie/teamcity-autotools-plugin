package jetbrains.buildServer.autotools.common;

/**
 * Created by naduxa on 11.07.2017.
 */
public final class AutotoolsBuildConstants {
  private AutotoolsBuildConstants(){
  }
  public static final String TYPE = "jetbrains-autotools-build";

  public static final String DESCRIPTION = "Using Autotools for configure and build project";

  public static final String DISPLAY_NAME = "Autotools configure and build";

  public static final String UI_PREFIX = "ui-" + TYPE + "-";

  public static final String UI_ADDITIONAL_CONF_PARAMS = UI_PREFIX + "additional-configure-params";
  public static final String UI_ADDITIONAL_MAKE_PARAMS = UI_PREFIX + "additional-make-params";

  public static final String UI_REDIRECT_STDERR = UI_PREFIX + "redirect-stderr";

  public static final String UI_NEED_AUTORECONF = UI_PREFIX + "need_autoreconf";

  public static final String UI_SOURCE_PATH = UI_PREFIX  + "sourse_path";



}
