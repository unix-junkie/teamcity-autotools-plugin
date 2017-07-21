package jetbrains.buildServer.autotools.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import javax.swing.text.StyledEditorKit;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.agent.AgentBuildRunner;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.SimpleBuildLogger;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import static jetbrains.buildServer.autotools.common.AutotoolsBuildConstants.*;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.FileUtil;
import org.apache.commons.logging.Log;
import org.jetbrains.annotations.NotNull;
import sun.tools.jar.CommandLine;

/**
 * Created by naduxa on 12.07.2017.
 */
public class AutotoolsBuildCLBService extends BuildServiceAdapter {

  private Set<File> myFilesToDelete = new HashSet<File>();
  private SimpleBuildLogger myLogger;
  @Override
  public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
    myLogger = getBuild().getBuildLogger();
    return makeCommandLineForCustomScript();
  }

  private String getVersion(){
    String version = "";
    try {
      File configure_ac = null;
      for (File file : getBuild().getCheckoutDirectory().listFiles()) {
        if (file.getName().equalsIgnoreCase("configure.ac") || file.getName().equalsIgnoreCase("configure.in")) {
          configure_ac = file;
          break;
        }
      }

      if (configure_ac == null) return version;

      Scanner scanner = new Scanner(configure_ac);
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        int idx = line.indexOf("AC_INIT");
        if (idx == -1)
          continue;
        idx += 7;
        int idx2 = line.indexOf(")");
        String ac_init = line.substring(idx, idx2);
        String[] params = ac_init.split(",");
        if (params.length < 2)
          break;
        idx = params[1].indexOf("["); idx2 = params[1].indexOf("]");
        if (idx == -1 || idx2 == -1)
          break;
        return version = params[1].substring(idx + 1, idx2);
      }

      return version;
    }
    catch (Exception e){
      return version;
    }
  }
  private String getArtifactName() {
    String artifactName = getBuild().getProjectName().replace(' ', '_') + "_" + getVersion().replace(' ', '_');
    return artifactName;
  }

  @NotNull
  protected ProgramCommandLine makeCommandLineForCustomScript() throws RunBuildException {
    String script = this.getCustomScript();
    enableExecution(script, this.getWorkingDirectory().getAbsolutePath());
    return this.createCommandLine(script, Collections.<String>emptyList());
  }

  private static void enableExecution(@NotNull String filePath, @NotNull String baseDir) {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath("chmod");
    commandLine.addParameter("+x");
    commandLine.addParameter(filePath);
    commandLine.setWorkDirectory(baseDir);
    ExecResult execResult = SimpleCommandLineProcessRunner.runCommand(commandLine, (byte[])null);
  }

  @NotNull
  protected String getCustomScript() throws RunBuildException {
    String scriptContent = this.getCustomScriptContent();

    String var10000;
    try {
      File scriptFile = File.createTempFile("custom_script", this.getCustomScriptExtension(), this.getAgentTempDirectory());
      FileUtil.writeFile(scriptFile, scriptContent, Charset.defaultCharset().name());
      this.myFilesToDelete.add(scriptFile);
      var10000 = scriptFile.getAbsolutePath();
    } catch (IOException var4) {
        RunBuildException exception = new RunBuildException("Failed to create temporary custom script file in directory '" + this.getAgentTempDirectory() + "': " + var4.toString(), var4);
        exception.setLogStacktrace(false);
        throw exception;
    }
    return var10000;
  }

  protected String getCheckProblemServiceMessage(String id){
    String message =   "code=$?\nif [ \"$code\" -eq 127 ]\nthen" +
    "\necho \"##teamcity[buildProblem description='" + id + " not available." + "' identity='" + id + "']\"\n exit\nfi"
                      + "\nif [ \"$code\" -ne 0 ]\nthen" +
    "\necho \"##teamcity[buildProblem description='" + id + " step failed." + "' identity='" + id + "']\"\n" + getFailedArtifactScript(id) + "\n exit\nfi";
    return message;
  }

  protected Boolean isNeededAutoreconf(){
    if (getRunnerParameters().get(UI_NEED_AUTORECONF) == null || !getRunnerParameters().get(UI_NEED_AUTORECONF).equalsIgnoreCase("true")) {
      myLogger.message("NEED_AUTORECONF: I WILL FIND");
      for (File file : getBuild().getCheckoutDirectory().listFiles()) {
        if (file.getName().equalsIgnoreCase("configure")) {
          return false;
        }
      }
    }
    return true;
  }

  @NotNull
  protected String getCustomScriptContent() throws RunBuildException {
    String config_params = "";
    String make_params = "";
    if (getRunnerParameters().get(UI_ADDITIONAL_CONF_PARAMS) != "" && getRunnerParameters().get(UI_ADDITIONAL_CONF_PARAMS) != null){
      config_params = " " + getRunnerParameters().get(UI_ADDITIONAL_CONF_PARAMS);
    }
    if (getRunnerParameters().get(UI_ADDITIONAL_MAKE_PARAMS) != null && getRunnerParameters().get(UI_ADDITIONAL_MAKE_PARAMS) != "") {
      make_params = " " + getRunnerParameters().get(UI_ADDITIONAL_MAKE_PARAMS);
    }
    String autoreconf = "autoreconf -ifs\n" + getCheckProblemServiceMessage("autoreconf");
    if (!isNeededAutoreconf()) {
      autoreconf = "";
    }

    String scriptContent = "#!/bin/sh\n" + autoreconf + "\n"
                           + "./configure" + config_params + "\n"
                           + getCheckProblemServiceMessage("configure") + "\n"
                           + "make " + make_params + "\n" + getCheckProblemServiceMessage("make") + "\n" +
                           "make DESTDIR=" +
                           getBuild().getBuildTempDirectory().getPath() + "/artifacts" + " install\n" +
                           getCheckProblemServiceMessage("make install")
                        + "\ncd " + getBuild().getBuildTempDirectory().getPath() + "/artifacts\n"
                           + "find * -type f -print > ../files.lst\n" +
                         "tar cvf  " + getArtifactName() + ".tar `cat ../files.lst`\n" + getCheckProblemServiceMessage("tar") +
                          "\ngzip -9 " + getArtifactName() + ".tar\n" + getCheckProblemServiceMessage("gzip") + "\n" +
                          "echo " + getSuccessArtifactMessage();
    Loggers.AGENT.info("XXX: script: " + scriptContent);
    return scriptContent;
  }

  protected String getSuccessArtifactMessage(){
    final String artifactMessage = "\"##teamcity[publishArtifacts '" + getBuild().getBuildTempDirectory().getPath() +  "/artifacts/" + getArtifactName() + ".tar.gz'" + "]\"";
    return artifactMessage;
  }

  protected String getFailedArtifactScript(String id){
    String script = "";
    if (id.equalsIgnoreCase("configure"))
      script = "\ncp config.log " + getBuild().getBuildTempDirectory().getPath() +  "/config.log"  + "\necho \"##teamcity[publishArtifacts '" + getBuild().getBuildTempDirectory().getPath() +  "/config.log']\"\n";
    //if (id.equalsIgnoreCase("make"))
    //  script = "\nfind . -type f -name ‘Makefile.*’ | xargs  tar -cvf " + getBuildTempDirectory().getPath() +  "/makefiles.tar\n" + "\ncd " + getBuildTempDirectory().getAbsolutePath() + "\ngzip -9 makefiles.tar\n";
    return script;
  }

  @NotNull
  protected ProgramCommandLine createCommandLine(@NotNull String exePath) {
    return this.createCommandLine(exePath, Collections.<String>emptyList());
  }

  @NotNull
  protected ProgramCommandLine createCommandLine(@NotNull String exePath, @NotNull List<String> arguments) {
    return new SimpleProgramCommandLine(this.getRunnerContext(), exePath, arguments);
  }


  public void afterProcessFinished() throws RunBuildException {
    super.afterProcessFinished();

    while (!this.myFilesToDelete.isEmpty()) {
      File file = myFilesToDelete.iterator().next();
      this.myFilesToDelete.remove(file);
      FileUtil.delete(file);
    }


  }

  @NotNull
  protected String getCustomScriptExtension() {
    return ".sh";
  }

}
