package jetbrains.buildServer.autotools.agent;

import com.google.common.annotations.VisibleForTesting;
import java.io.*;
import java.util.Scanner;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 18.08.2017.
 * Author     : Nadezhda Demina
 */
public class DejagnuTestsXMLParser {
  /**
   * Flag to need replace &[^;] to &amp in xml
   */
  private final boolean myNeedReplaceApm;
  private int myTestsCount;
  /**
   * Flag to replace Controls charecters;
   */
  private final boolean myNeedToReplaceControls;

  private final AutotoolsTestsReporter myTestsReporter;

  public DejagnuTestsXMLParser(final AutotoolsTestsReporter testsReporter, final Boolean needReplaceApm,final Boolean needToReplaceControls){
    myTestsReporter = testsReporter;
    myNeedReplaceApm = needReplaceApm;
    myNeedToReplaceControls = needToReplaceControls;
  }

  /**
   * Handles Xml file and public dejagnu test results from this
   *  @param xmlFile file with test results
   * @return count of public xmlFile has been handled
   */
  int handleXmlResults(@NotNull final File xmlFile){
    myTestsCount = 0;
    try {
      String xmlEntry = new Scanner(xmlFile, "UTF-8").useDelimiter("\\A").next();
      if (myNeedReplaceApm){
        xmlEntry = replaceAmp(xmlEntry.replaceFirst("<\\?xml version=\"1.0\"", "<?xml version=\"1.1\""));
      }
      if (myNeedToReplaceControls){
        xmlEntry = replaceControlChars(xmlEntry);
      }
      parseXmlTestResults(xmlEntry, xmlFile.getAbsolutePath());
      return myTestsCount;
    }
    catch (final FileNotFoundException e){
      //e.getMessage();
      return 0;
    }
  }

  /**
   * Replace control charecrters in string and return new string.
   * @param xmlEntry string for replacing
   * @return new string after replacing
   */
  @NotNull
  private String replaceControlChars(@NotNull final String xmlEntry) {
    final StringBuilder tempEntry = new StringBuilder();
    for (int i = 0; i < xmlEntry.length(); i++){
      if ((xmlEntry.charAt(i) >= 0x0000 && xmlEntry.charAt(i) <= 0x001f ||
          xmlEntry.charAt(i) >= 0xc2a0 && xmlEntry.charAt(i) <= 0x001f) &&
          xmlEntry.charAt(i) != 0x000a && xmlEntry.charAt(i) != 0x0009 && xmlEntry.charAt(i) != 0x000d &&
          xmlEntry.charAt(i) != 0x0085 && xmlEntry.charAt(i) != 0x2028) {
          tempEntry.append("&#x" + Integer.toHexString( (int)xmlEntry.charAt(i)) + ";");

      }
      else{
        tempEntry.append(xmlEntry.charAt(i));
      }
    }
    return tempEntry.toString();
  }

  /**
   * Parses Xml file entry and public test results
   * @param xmlEntry Xml file entry
   * @param testSuiteName name of test suite
   * @return true if was success parsing
   */
  @VisibleForTesting
  void parseXmlTestResults(@NotNull final String xmlEntry, @NotNull final String testSuiteName){
    Boolean isTestXml = false;
    try {
      final XMLInputFactory f = XMLInputFactory.newInstance();
      final XMLStreamReader reader = f.createXMLStreamReader(new StringReader(xmlEntry));
      String testResult = null;
      String testName = null;
      String tempTag = "";
      String testOutput = "";
      while (reader.hasNext()){
        if (!isTestXml && reader.hasName()){
          if (!reader.getLocalName().equalsIgnoreCase("testsuite")){
            break;
          }
          isTestXml = true;
          if (myTestsReporter != null) {
            myTestsReporter.publicTestSuiteStarted(testSuiteName);
          }
        }
        if (reader.isStartElement() && reader.hasName() && reader.getLocalName().equalsIgnoreCase("test")){
          testResult = null;
          testName = null;
        }
        if (reader.isEndElement() && reader.hasName() && reader.getLocalName().equalsIgnoreCase("test")){
          myTestsCount += testResult != null && testName != null ? 1 : 0;
          if (myTestsReporter != null && testResult != null && testName != null){
            myTestsReporter.publicTestCaseInfo(testName, testResult, testOutput);
          }
        }

        if (reader.hasName()){
          tempTag = reader.getLocalName();
        }
        if (reader.hasText()){
          if (tempTag.equalsIgnoreCase("result")){
            testResult = reader.getText();
          }
          else {
            if (tempTag.equalsIgnoreCase("name")) {
              testName = reader.getText();
            } else if (tempTag.equalsIgnoreCase("output")) {
              testOutput = reader.getText();
            }
          }
        }
        reader.next();
      }
      reader.close();
    } catch (final XMLStreamException e) {
      myTestsCount = 0;
      if (myTestsReporter != null) {
        myTestsReporter.reportWarning("DejagnuTestsXMLParser:" + e.getMessage());
      }
      else{
        System.out.println("DejagnuTestsXMLParser: " + e.getMessage());
      }
    }
    finally {
        if (isTestXml && myTestsReporter != null){
        myTestsReporter.publicTestSuiteFinished(testSuiteName);
      }
    }
  }

  /**
   * Get string from xmlFile and replace invalid charecters.
   *
   * @param xmlFile File with Xml
   * @return string with update xml data
   */
  @NotNull
  private String replaceAmp(@NotNull final String xmlEntry) {
    return xmlEntry.replaceAll("&[^;]", "&#x26;");
  }
}
