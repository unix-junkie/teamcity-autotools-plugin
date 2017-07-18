package jetbrains.buildServer.autotools.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
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

/**
 * Created by naduxa on 12.07.2017.
 */
public class AutotoolsBuildCLBService extends BuildServiceAdapter {

  private Set<File> myFilesToDelete = new HashSet<File>();
  private SimpleBuildLogger myLogger;
  @Override
  public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
    return makeCommandLineForCustomScript();
  }

  private String getArtifactName(){
    String artifactName = getBuild().getProjectName();
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

    String autoreconf = "autoreconf -ifs\n";
    for (File file: getBuild().getCheckoutDirectory().listFiles()){
      if (file.getName().equalsIgnoreCase("configure")) {
        autoreconf = "";
        break;
      }
    }

    String scriptContent = "#!/bin/sh\nset -e\n" + autoreconf + "./configure" + config_params + "\n"
                           + "make" + make_params + "\n" +
                           "make DESTDIR=" +
                           getBuild().getBuildTempDirectory().getPath() + "/artifacts" + " install\n"
                        + "cd " + getBuild().getBuildTempDirectory().getPath() + "/artifacts\n"
                           + "find * -type f -print > ../files.lst\n" +
                         "tar cvf  " + getArtifactName() + ".tar `cat ../files.lst`\n" +
                          "gzip -9 " + getArtifactName() + ".tar";
    Loggers.AGENT.info("XXX: script: " + scriptContent);

    return scriptContent;
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
    myLogger = getBuild().getBuildLogger();
    String artifactMessage = "##teamcity[publishArtifacts " +"\'" + getBuild().getBuildTempDirectory().getPath() +  "/artifacts/" + getArtifactName() + ".tar.gz\'" + "]";
    myLogger.message(artifactMessage);

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
