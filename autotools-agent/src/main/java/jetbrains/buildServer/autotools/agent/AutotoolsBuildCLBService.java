package jetbrains.buildServer.autotools.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import static jetbrains.buildServer.autotools.common.AutotoolsBuildConstants.*;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 12.07.2017.
 * @author     : Nadezhda Demina
 */
public final class AutotoolsBuildCLBService extends BuildServiceAdapter {
  /**
   * Collection of work files be deleted in the end.
   */
  private final Collection<File> myFilesToDelete = new HashSet<File>();
  @Override
  @NotNull
  public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
    return makeCommandLineForScript();
  }

  /**
   * Returns version of temp project.
   *<P>
   * @return version of temp project
   */
  @NotNull
  private String getVersion(){
    File configure_ac = null;
    for (final File file : getBuild().getCheckoutDirectory().listFiles()) {
      if (file.getName().equalsIgnoreCase("configure.ac") || file.getName().equalsIgnoreCase("configure.in")) {
        configure_ac = file;
        break;
      }
    }
    if (configure_ac == null) {
      return "";
    }
    try {
      final Scanner scanner = new Scanner(configure_ac);
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine();
        int idx = line.indexOf("AC_INIT");
        if (idx == -1) {
          continue;
        }
        idx += 7;
        int idx2 = line.indexOf(")");
        final String ac_init = line.substring(idx, idx2);
        final String[] params = ac_init.split(",");
        if (params.length < 2) {
          break;
        }
        idx = params[1].indexOf("["); idx2 = params[1].indexOf("]");
        if (idx == -1 || idx2 == -1) {
          break;
        }
        return params[1].substring(idx + 1, idx2);
      }
      return "";
    }
    catch (final Exception e){
      return "";
    }
  }

  /**
   * Returns name of artifact of successful build.
   * @return name of artifact
   */
  @NotNull
  private String getArtifactName() {
    final String artifactName = getBuild().getProjectName().replace(' ', '_') + "_" + getVersion().replace(' ', '_');
    return artifactName;
  }

  /**
   *  Returns commandLine when need to execute script.
   * @return commindLine with script
   * @throws RunBuildException if ssetting executable attribute for script be failed
   */
  @NotNull
  private ProgramCommandLine makeCommandLineForScript() throws RunBuildException {
    final String script = getScript();
    enableExecution(script, getWorkingDirectory().getAbsolutePath());
    return createCommandLine(script, Collections.<String>emptyList());
  }

  /**
   * Set executable attribute for file
   * @param filePath File to be setted executable attribute
   * @param baseDir Directory to be setted Work Directory
   */
  private static void enableExecution(@NotNull final String filePath, @NotNull final String baseDir) {
    final GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath("chmod");
    commandLine.addParameter("+x");
    commandLine.addParameter(filePath);
    commandLine.setWorkDirectory(baseDir);
    final ExecResult execResult = SimpleCommandLineProcessRunner.runCommand(commandLine, (byte[])null);
    if(execResult.getExitCode() != 0) {
      Loggers.AGENT.warn("Failed to set executable attribute for " + filePath + ": chmod +x exit code is " + execResult.getExitCode());
    }
  }

  /**
   * Creates a script file in AgentTempDirectory and returns absolute path.
   * @return path of script file
   * @throws RunBuildException if failed to create temporary script file in directory
   */
  @NotNull
  private String getScript() throws RunBuildException {
    final String scriptContent = getScriptContent();

    final String var10000;
    try {
      final File scriptFile = File.createTempFile("build_script", getScriptExtension(), getAgentTempDirectory());
      FileUtil.writeFile(scriptFile, scriptContent, Charset.defaultCharset().name());
      myFilesToDelete.add(scriptFile);
      var10000 = scriptFile.getAbsolutePath();
    } catch (final IOException var4) {
        final RunBuildException exception = new RunBuildException("Failed to create temporary build script file in directory '" + getAgentTempDirectory() + "': " + var4.toString(), var4);
        exception.setLogStacktrace(false);
        throw exception;
    }

    return var10000;
  }


  /**
   * It identifies the need  to execute autoreconf
   * @return true, if it is needed to execute autoreconf
   */
  @NotNull
  private Boolean isNeededAutoreconf(){
    if (getRunnerParameters().get(UI_NEED_AUTORECONF) == null || !getRunnerParameters().get(UI_NEED_AUTORECONF).equalsIgnoreCase("true")) {
      for (final File file : getBuild().getCheckoutDirectory().listFiles()) {
        if (file.getName().equalsIgnoreCase("configure")) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Addes needed enviroment variblies for build script.
   */
  private void addMyEnviroventVariblies(){
    String sourcePath = getRunnerParameters().get(UI_SOURCE_PATH) == null ? "" : getRunnerParameters().get(UI_SOURCE_PATH);
    sourcePath = (sourcePath != "" && sourcePath.charAt(0) != '/') ? "/" + sourcePath : sourcePath;
    getBuild().addSharedEnvironmentVariable("SOURCE_PATH", getBuild().getCheckoutDirectory().getAbsolutePath() + sourcePath);
    getBuild().addSharedEnvironmentVariable("ARTIFACT_NAME", getArtifactName());
    getBuild().addSharedEnvironmentVariable("LAST_STEP", "9");

    if (!getRunnerParameters().containsKey(UI_MAKE_CHECK) || getRunnerParameters().get(UI_MAKE_CHECK) == null){
      getBuild().addSharedEnvironmentVariable(UI_MAKE_CHECK, "check");
    }
    else
      getBuild().addSharedEnvironmentVariable(UI_MAKE_CHECK, getRunnerParameters().get(UI_MAKE_CHECK));

    if (isNeededAutoreconf()) {
      getBuild().addSharedEnvironmentVariable("NEED_AUTORECONF", "1");
    }
    else{
      getBuild().addSharedEnvironmentVariable("NEED_AUTORECONF", "0");
    }
    if (getRunnerParameters().get(UI_ADDITIONAL_CONF_PARAMS) != null && !getRunnerParameters().get(UI_ADDITIONAL_CONF_PARAMS).equalsIgnoreCase("")) {
      getBuild().addSharedEnvironmentVariable("CONF_PARAMS", getRunnerParameters().get(UI_ADDITIONAL_CONF_PARAMS));
    }
    if (getRunnerParameters().get(UI_ADDITIONAL_MAKE_PARAMS) != null && !getRunnerParameters().get(UI_ADDITIONAL_MAKE_PARAMS).equalsIgnoreCase("")) {
      getBuild().addSharedEnvironmentVariable("MK_PARAMS", getRunnerParameters().get(UI_ADDITIONAL_MAKE_PARAMS));
    }
    if (getRunnerParameters().get(HAS_RUNTEST_VAR) != null && getRunnerParameters().get(HAS_RUNTEST_VAR).equalsIgnoreCase("1")){
      getBuild().addSharedEnvironmentVariable(HAS_RUNTEST_VAR, "1");
      if (getRunnerParameters().get(RUNTEST_XML_FILE_VAR).equalsIgnoreCase("false")){
        getBuild().addSharedEnvironmentVariable(RUNTEST_XML_FILE_VAR, "=\"testresults.xml\"");
      }
    }
  }

  /**
   * Returns  content of script for build steps (autoreconf, ./configure, make, make install),
   * @return content of script
   */
  @NotNull
  private String getScriptContent() throws RunBuildException {
    addMyEnviroventVariblies();
    String script = "";
    try {
      final BufferedReader bufferead = new BufferedReader(
        new InputStreamReader(getClass().getResourceAsStream("/build_script.sh"), Charset.forName("UTF-8")));
      String str = "";
      while ((str = bufferead.readLine()) != null) {
        script += str + "\n";
      }
      bufferead.close();
    }
    catch (IOException e){
      final RunBuildException exception = new RunBuildException("Failed to read temporary build script file in plugin resources.");
      exception.setLogStacktrace(false);
      throw exception;
    }
    return script;
  }

  /**
   * Creates command Line with content exePath and list of arguments.
   * @param exePath  path to execute
   * @param arguments arguments for execution
   * @return ProgramCommandLine
   */
  @NotNull
  private ProgramCommandLine createCommandLine(@NotNull final String exePath, @NotNull final List<String> arguments) {
    return new SimpleProgramCommandLine(getRunnerContext(), exePath, arguments);
  }

  @Override
  public void afterProcessFinished() throws RunBuildException {


    super.afterProcessFinished();
    while (!myFilesToDelete.isEmpty()) {
      final File file = myFilesToDelete.iterator().next();
      myFilesToDelete.remove(file);
      FileUtil.delete(file);
    }
  }

  /**
   * Returns extension of srcipt
   * @return
   */
  @NotNull
  private static String getScriptExtension() {
    return ".sh";
  }

}
