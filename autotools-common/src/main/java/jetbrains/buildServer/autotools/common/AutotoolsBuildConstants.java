package jetbrains.buildServer.autotools.common;

/**
 * Created by naduxa on 11.07.2017.
 */
public final class AutotoolsConfigureConstants {
  private AutotoolsConfigureConstants(){
  }
  public static final String TYPE = "jetbrains-autotools-conf";

  public static final String DESCRIPTION = "Using Autotools for configure project";

  public static final String DISPLAY_NAME = "Autotools configure";

  public static final String UI_PREFIX = "ui-" + TYPE + "-";

  public static final String UI_ADDITIONAL_PARAMS = UI_PREFIX + "additional-cmd-params";

  public static final String UI_REDIRECT_STDERR = UI_PREFIX + "redirect-stderr";

  public static final String UI_NEED_AUTORECONF = UI_PREFIX + "need_autoreconf";

  public static final String UI_SOURCE_PATH = UI_PREFIX  + "sourse_path";


}
