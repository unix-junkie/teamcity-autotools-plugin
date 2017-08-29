package jetbrains.buildServer.autotools.agent;

import com.intellij.openapi.util.Pair;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.Test;

import static jetbrains.buildServer.autotools.common.AutotoolsBuildConstants.HAS_RUNTEST_VAR;
import static jetbrains.buildServer.autotools.common.AutotoolsBuildConstants.MY_RUNTESTFLAGS;


/**
 * <p>Test Class for jetbrains.buildServer.autotools.agent package.</p>
 *
 * @author Nadezhda Demina
 */
public final class AutotoolsAgentTest {

  @Test
  public void isThisExtensionFileTest(){
    Assert.assertTrue(AutotoolsTestsReporter.isThisExtensionFile(new File("fortest.log"), ".log"));
    Assert.assertFalse(AutotoolsTestsReporter.isThisExtensionFile(new File("fortest.log"), "log"));
    Assert.assertTrue(AutotoolsTestsReporter.isThisExtensionFile(new File("for.test.log"), ".log"));
  }

  @Test
  public void findVersionTest(){
    final String text1 = "Expect version is       5.45\n" +
                  "Tcl version is          8.5\n" +
                  "Framework version is    1.4.4";
    Assert.assertEquals(new RuntestToolProvider("runtest", "--version").findVersion(text1), "1.4.4", "text message: " + text1);

    final String text2 = "Expect version is       5.45\n" +
           "Tcl version is          8.6\n" +
           "Framework version is    1.5.1";
    Assert.assertEquals(new RuntestToolProvider("runtest", "--version").findVersion(text2), "1.5.1", "text message: " + text2);

    final String text3 = "Expect version is       5.45\n" +
           "Tcl version is          8.6\n" +
           "Framework version is    1.5.3";
    Assert.assertEquals(new RuntestToolProvider("runtest", "--version").findVersion(text3), "1.5.3", "text message: " + text3);

    final String text4 = "DejaGnu version 1.6\n" +
           "Expect version  5.45\n" +
           "Tcl version     8.6";
    Assert.assertEquals(new RuntestToolProvider("runtest", "--version").findVersion(text4), "1.6", "text message: " + text4);

  }

  @Test
  public void replaceXmlHeaderVersionTest(){
    Assert.assertEquals(DejagnuTestsXMLParser.replaceXmlHeaderVersion("<?xml version=\"1.0\"?>"), "<?xml version=\"1.1\"?>");
    Assert.assertEquals(DejagnuTestsXMLParser.replaceXmlHeaderVersion("<?xml\n version=\"1.0\"?>"), "<?xml\n version=\"1.1\"?>");
    Assert.assertEquals(DejagnuTestsXMLParser.replaceXmlHeaderVersion("<?xml version=\"1.0\"        ?>"), "<?xml version=\"1.1\"        ?>");
    Assert.assertEquals(DejagnuTestsXMLParser.replaceXmlHeaderVersion("<?xml version=\"1.0\"\n\n?>"), "<?xml version=\"1.1\"\n\n?>");
    Assert.assertEquals(DejagnuTestsXMLParser.replaceXmlHeaderVersion("<?xml version=\"1.0\"encoding=\"utf-8\"?>\n"), "<?xml version=\"1.1\"encoding=\"utf-8\"?>\n");

  }
  @Test
  public void getDejagnuParametersTest(){
    final Collection<Pair<String, String>> answer = new ArrayList<Pair<String, String>>();
    answer.add(new Pair<String, String>(HAS_RUNTEST_VAR, "1"));
    answer.add(new Pair<String, String>(MY_RUNTESTFLAGS, "RUNTESTFLAGS=--all"));
    Assert.assertEquals(RuntestToolProvider.getDejagnuParameters("1.4.4"), answer,"for version 1.4.4");
    answer.clear();

    answer.add(new Pair<String, String>(HAS_RUNTEST_VAR, "1"));
    answer.add(new Pair<String, String>(MY_RUNTESTFLAGS, "RUNTESTFLAGS=--all --xml"));
    Assert.assertEquals(RuntestToolProvider.getDejagnuParameters("1.5.1"), answer, "for version 1.5.1");
    answer.clear();

    answer.add(new Pair<String, String>(HAS_RUNTEST_VAR, "1"));
    //answer.add(new Pair<String, String>(MY_RUNTESTFLAGS, "'--all --xml'"));
    answer.add(new Pair<String, String>(MY_RUNTESTFLAGS, "RUNTESTFLAGS=--all --xml=\"testresults.xml\""));
    Assert.assertEquals(RuntestToolProvider.getDejagnuParameters("1.5.3"), answer, "for version 1.5.3");
    Assert.assertEquals(RuntestToolProvider.getDejagnuParameters("1.6"), answer, "for version 1.6");
  }
  @Test
  public void getRelativePathTest(){
    final Mockery mockery = new Mockery();
    final BuildProgressLogger logger = mockery.mock(BuildProgressLogger.class);
    final AutotoolsTestsReporter testReporter = new AutotoolsTestsReporter(logger, "C:/Users/naduxa/someprojects/teamcity-autotools-plugin/autotools-agent/src/test");
   Assert.assertEquals(testReporter.getRelativePath("C:/Users/naduxa/someprojects/teamcity-autotools-plugin/autotools-agent/src/test/resources/testng.xml"),
                        "/resources/testng.xml");
  }

  @Test
  public void replaceAmpTest(){
    Assert.assertEquals(DejagnuTestsXMLParser.replaceAmp("Punch &"), "Punch &#x26;");
    Assert.assertEquals(DejagnuTestsXMLParser.replaceAmp("Punch & Judy"), "Punch &#x26; Judy");
    Assert.assertEquals(DejagnuTestsXMLParser.replaceAmp("Punch &amp; Judy"), "Punch &amp; Judy");
    Assert.assertEquals(DejagnuTestsXMLParser.replaceAmp("Punch &#x26; Judy"), "Punch &#x26; Judy");
  }

  @Test
  public void stringArrayToStringTest(){
    final String text = "DejaGnu version 1.6\n" +
           "Expect version  5.45\n" +
           "Tcl version     8.6";
    Assert.assertEquals(AutotoolsToolProvider.stringArrayToString(text.split("\n")), text);
  }

  @Test
  public void dejagnuXmlPasringTest() {
    final URL resource = getClass().getClassLoader().getResource("dejagnu-xml-output");
    if (resource == null){
      Assert.fail("DejagnuXmlPasringTest: Not found dejagnu-xml-output folder");
    }
    final String file = resource.getFile();
    if (file == null){
      Assert.fail("DejagnuXmlPasringTest: Not found dejagnu-xml-output folder");
    }
    final File dejgnuOutput = new File(file);
    if (!dejgnuOutput.isDirectory()){
      Assert.fail("DejagnuXmlPasringTest: Not found dejagnu-xml-output folder");
    }
    final Map<String, Integer> testCount = new HashMap<String, Integer>();
    testCount.put("binutils.xml", 173);
    testCount.put("correctResults.xml", 10);
    testCount.put("gas.xml", 595);
    testCount.put("ld.xml", 1698);
    final File[] files = dejgnuOutput.listFiles();
    if (files == null){
      return;
    }
    for (final File xmlFile : files) {
      if (testCount.containsKey(xmlFile.getName())) {
        Assert.assertEquals(new DejagnuTestsXMLParser(null, true, true).handleXmlResults(xmlFile),
                            testCount.get(xmlFile.getName()).intValue(), "test for file " + xmlFile.getName());
      } else {
        Assert.assertTrue(new DejagnuTestsXMLParser(null, true, true).handleXmlResults(xmlFile) > 1000,
                          "test for file " + xmlFile.getName());
      }
    }
  }
}
