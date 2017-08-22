package jetbrains.buildServer.autotools.agent; /**
 * Created on 08.08.2017.
 * Author     : Nadezhda Demina
 */

import com.intellij.openapi.util.Pair;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;
import static jetbrains.buildServer.autotools.common.AutotoolsBuildConstants.*;


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
  public void findVersionTest(){
    String text = "Expect version is       5.45\n" +
                  "Tcl version is          8.5\n" +
                  "Framework version is    1.4.4";
    Assert.assertEquals((new RuntestToolProvider("runtest", "--version")).findVersion(text), "1.4.4");

    text = "Expect version is       5.45\n" +
           "Tcl version is          8.6\n" +
           "Framework version is    1.5.1";
    Assert.assertEquals((new RuntestToolProvider("runtest", "--version")).findVersion(text), "1.5.1");

    text = "Expect version is       5.45\n" +
           "Tcl version is          8.6\n" +
           "Framework version is    1.5.3";
    Assert.assertEquals((new RuntestToolProvider("runtest", "--version")).findVersion(text), "1.5.3");

    text = "DejaGnu version 1.6\n" +
           "Expect version  5.45\n" +
           "Tcl version     8.6";
    Assert.assertEquals((new RuntestToolProvider("runtest", "--version")).findVersion(text), "1.6");

  }

  @Test
  public void getDejagnuParametersTest(){
    List<Pair<String, String>> answer = new ArrayList<Pair<String, String>>();
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
   Assert.assertEquals(new AutotoolsTestsReporter("C:/Users/naduxa/someprojects/teamcity-autotools-plugin/autotools-agent/src/test").getRelativePath("C:/Users/naduxa/someprojects/teamcity-autotools-plugin/autotools-agent/src/test/resources/testng.xml"),
                        "/resources/testng.xml");
  }

  @Test
  public void stringArrayToStringTest(){
    String text = "DejaGnu version 1.6\n" +
           "Expect version  5.45\n" +
           "Tcl version     8.6";
    Assert.assertEquals(AutotoolsToolProvider.stringArrayToString(text.split("\n")), text);
  }

  @Test
  public void DejagnuXmlPasringTest() throws IOException {
    File dejgnuOutput =  new File(getClass().getClassLoader().getResource("dejagnu-xml-output").getFile());
    if (!dejgnuOutput.isDirectory()){
      Assert.fail();
    }
    Map<String, Integer> testCount = new HashMap<String, Integer>();
    testCount.put("binutils.xml", 173);
    testCount.put("correctResults.xml", 10);
    testCount.put("gas.xml", 595);
    testCount.put("ld.xml", 1698);
    for (File xmlFile : dejgnuOutput.listFiles()){
      if (testCount.containsKey(xmlFile.getName())){
        Assert.assertEquals(new DejagnuTestsXMLParser(null, true, true).handleXmlResults(xmlFile),
                            (int)testCount.get(xmlFile.getName()), "test for file " + xmlFile.getName());
      }
      else{
        Assert.assertTrue(new DejagnuTestsXMLParser(null, true, true).handleXmlResults(xmlFile) > 1000,
                          "test for file " + xmlFile.getName());
      }
    }
  }
}
