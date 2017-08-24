package jetbrains.buildServer.autotools.common;

/**
 * Created on 11.07.2017.
 * Author     : Nadezhda Demina
 */
public final class AutotoolsBuildConstants {
  /**
   * This private constuctor is needed for an instance of a class can not be created.
   */
  private AutotoolsBuildConstants(){
    assert false;
  }

  /**
   * This constant is for runner type name.
   */
  public static final String TYPE = "jetbrains-autotools-build";

  /**
   *This constant is for runner plugin description name.
   */
  public static final String DESCRIPTION = "Using Autotools for configure and build project";

  /**
   * This constant is for runner plugin name for UI.
   */
  public static final String DISPLAY_NAME = "Autotools configure and build";

  /**
   * This constant is for prefix UI elements plugin names.
   */
  private static final String UI_PREFIX = "ui-" + TYPE + "-";

  /**
   * This constant is for UI element name of configure script arguments.
   */
  public static final String UI_ADDITIONAL_CONF_PARAMS = UI_PREFIX + "additional-configure-params";
  /**
   * This constant is for UI element name of make arguments.
   */
  public static final String UI_ADDITIONAL_MAKE_PARAMS = UI_PREFIX + "additional-make-params";

  /**
   * This constant is for UI element name of autoreconf need.
   */
  public static final String UI_NEED_AUTORECONF = UI_PREFIX + "need_autoreconf";

  public static final String UI_DEJAGNU_XML_REPLACE_AMP= UI_PREFIX + "dejagnu_xml_replace_apm";

  public static final String UI_DEJAGNU_XML_REPLACE_CONTROLS= UI_PREFIX + "dejagnu_xml_replace_controls";
  /**
   *  This constant is for UI element name of source path.
   */
  public static final String UI_SOURCE_PATH = UI_PREFIX  + "source_path";


  public static final String HAS_RUNTEST_VAR = "HAS_RUNTEST";
  public static final String MY_RUNTESTFLAGS = "MY_RUNTESTFLAGS";
  public static final String UI_MAKE_CHECK = "MAKE_CHECK";
  public static final String TOOL_MAKE = "make";
  public static final String TOOL_AUTOCONF = "autoconf";
  public static final String TOOL_TAR = "tar";
  public static final String TOOL_GZIP = "gzip";
  public static final String TOOL_RUNTEST = "runtest";

}
