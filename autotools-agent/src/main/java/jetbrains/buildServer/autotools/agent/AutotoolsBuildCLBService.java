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
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 12.07.2017.
 * @author     : Nadezhda Demina
 */
final class AutotoolsBuildCLBService extends BuildServiceAdapter {
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
    final File[] files = getBuild().getCheckoutDirectory().listFiles();
    if (files == null) {
      return "";
    }

    File configure_ac = null;
    for (final File file : files) {
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
        final int idx2 = line.indexOf(')');
        final String ac_init = line.substring(idx, idx2);
        final String[] params = ac_init.split(",");
        if (params.length < 2) {
          break;
        }
        final int openBracketsIdx = params[1].indexOf('[');
        final int closeBracketsIdx = params[1].indexOf(']');
        if (openBracketsIdx == -1 || closeBracketsIdx == -1) {
          break;
        }
        return params[1].substring(openBracketsIdx + 1, closeBracketsIdx);
      }
      return "";
    }
    catch (final Exception ignored){
      return "";
    }
  }

  /**
   * Returns name of artifact of successful build.
   * @return name of artifact
   */
  @NotNull
  private String getArtifactName() {
    return getBuild().getProjectName().replace(' ', '_') + '_' + getVersion().replace(' ', '_');
  }

  /**
   *  Returns commandLine when need to execute script.
   * @return commindLine with script
   * @throws RunBuildException if ssetting executable attribute for script be failed
   */
  @NotNull
  private ProgramCommandLine makeCommandLineForScript() throws RunBuildException {
    addMyEnviroventVariblies();
    final String script = getScript();
    enableExecution(script, getWorkingDirectory().getAbsolutePath());
    return createCommandLine(script, Collections.<String>emptyList());
  }

  /**
   * Set executable attribute for file.
   * @param filePath File to be setted executable attribute
   * @param baseDir Directory to be setted Work Directory
   */
  private static void enableExecution(@NotNull final String filePath, @NotNull final String baseDir) {
    final GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath("chmod");
    commandLine.addParameter("+x");
    commandLine.addParameter(filePath);
    commandLine.setWorkDirectory(baseDir);
    final ExecResult execResult = SimpleCommandLineProcessRunner.runCommand(commandLine, null);
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
    try {
      final File scriptFile = File.createTempFile("build_script", getScriptExtension(), getAgentTempDirectory());
      FileUtil.writeFile(scriptFile, scriptContent, Charset.defaultCharset().name());
      myFilesToDelete.add(scriptFile);
      return scriptFile.getAbsolutePath();
    } catch (final IOException var4) {
        final RunBuildException exception = new RunBuildException("Failed to create temporary build script file in directory '" + getAgentTempDirectory() + "': " + var4, var4);
        exception.setLogStacktrace(false);
        throw exception;
    }
  }


  /**
   * It identifies the need  to execute autoreconf.
   * @return true, if it is needed to execute autoreconf
   */
  private Boolean isNeededAutoreconf(){
    if (getRunnerParameters().get(UI_NEED_AUTORECONF) == null || !getRunnerParameters().get(UI_NEED_AUTORECONF).equalsIgnoreCase("true")) {
      final File[] files = getBuild().getCheckoutDirectory().listFiles();
      if (files == null){
        return true;
      }
      for (final File file : files) {
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
    sourcePath = !sourcePath.isEmpty() && sourcePath.charAt(0) != '/'
                 ? '/' + sourcePath
                 : sourcePath;
    getBuild().addSharedEnvironmentVariable("SOURCE_PATH", getBuild().getCheckoutDirectory().getAbsolutePath() + sourcePath);
    getBuild().addSharedEnvironmentVariable("ARTIFACT_NAME", getArtifactName());
    getBuild().addSharedEnvironmentVariable("LAST_STEP", "9");

    if (!getRunnerParameters().containsKey(UI_MAKE_CHECK) || getRunnerParameters().get(UI_MAKE_CHECK) == null){
      getBuild().addSharedEnvironmentVariable(UI_MAKE_CHECK, "check");
    }
    else {
      getBuild().addSharedEnvironmentVariable(UI_MAKE_CHECK, getRunnerParameters().get(UI_MAKE_CHECK));
    }

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
  }

  /**
   * Returns  content of script for build steps (autoreconf, ./configure, make, make install),
   * @return content of script
   * @throws RunBuildException if an {@link IOException} is thrown when reading
   *         the build script.
   */
  @NotNull
  private String getScriptContent() throws RunBuildException{
    try{
      return getScriptContent0();
    }
    catch (final IOException e){
      throw new RunBuildException(e);
    }
  }

  @SuppressWarnings("NestedAssignment")
  @NotNull
  private String getScriptContent0() throws IOException {
    final BufferedReader bufferead = new BufferedReader(
      new InputStreamReader(getClass().getResourceAsStream("/build_script.sh"), Charset.forName("UTF-8")));;
    try {
      final StringBuilder script = new StringBuilder();
      String str;
      while ((str = bufferead.readLine()) != null) {
        script.append(str).append('\n');
      }
      return script.toString();
    }
    finally {
        bufferead.close();
    }
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
   * Returns extension of script.
   * @return the extension of the build shell script.
   */
  @NotNull
  private static String getScriptExtension() {
    return ".sh";
  }

}
