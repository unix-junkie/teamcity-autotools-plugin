package jetbrains.buildServer.autotools.agent; /**
 * Created on 08.08.2017.
 * Author     : Nadezhda Demina
 */

import java.io.File;
import jetbrains.buildServer.autotools.agent.AutotoolsTestsReporter;
import jetbrains.buildServer.autotools.agent.AutotoolsLifeCycleListener;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test Class for jetbrains.buildServer.autotools.agent package
 */
public class AutotoolsAgentTest {

  @Test
  public void isThisExtensionFileTest(){
    Assert.assertTrue(AutotoolsTestsReporter.isThisExtensionFile(new File("fortest.log"), ".log"));
    Assert.assertFalse(AutotoolsTestsReporter.isThisExtensionFile(new File("fortest.log"), "log"));
    Assert.assertTrue(AutotoolsTestsReporter.isThisExtensionFile(new File("for.test.log"), ".log"));
  }


  @Test
  public void compareVersionsTest(){
    Assert.assertFalse(AutotoolsLifeCycleListener.compareVersions("1.1", "1"));
  }
}