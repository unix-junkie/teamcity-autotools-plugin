package jetbrains.buildServer.autotools.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.xpath.SourceTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Nadezhda Demina
 */
final class DejagnuTestsXMLParser {
  /**
   * Flag to need replace &[^;] to &amp in xml.
   */
  private final boolean myNeedReplaceApm;
  private static final Pattern XML_HEADER_PATTERN = Pattern.compile("(\\<\\?xml\\s+version\\s*\\=\\s*\\\")(1\\.0)(\\\")", Pattern.DOTALL);
  private int myTestsCount;
  /**
   * Flag to replace Controls charecters;.
   */
  private final boolean myNeedToReplaceControls;

  @Nullable
  private final AutotoolsTestsReporter myTestsReporter;

  DejagnuTestsXMLParser(@Nullable final AutotoolsTestsReporter testsReporter, final Boolean needReplaceApm, final Boolean needToReplaceControls){
    myTestsReporter = testsReporter;
    myNeedReplaceApm = needReplaceApm;
    myNeedToReplaceControls = needToReplaceControls;
  }




  /**
   * Handles Xml file and public dejagnu test results from this.
   *
   * @param xmlFile file with test results
   * @return count of public xmlFile has been handled
   */
  int handleXmlResults(@NotNull final File xmlFile){
    myTestsCount = 0;
    try {
      String xmlEntry = new Scanner(xmlFile, "UTF-8").useDelimiter("\\A").next();
      if (myNeedReplaceApm){
        xmlEntry = replaceAmp(replaceXmlHeaderVersion(xmlEntry));
      }
      if (myNeedToReplaceControls){
        xmlEntry = replaceControlChars(xmlEntry);
      }
      parseXmlTestResults(xmlEntry, xmlFile.getAbsolutePath());
      return myTestsCount;
    }
    catch (final FileNotFoundException ignored){
      //e.getMessage();
      return 0;
    }
  }

  /**
   * Replace header with xml version 1.0 to version 1.1.
   * @param xmlEntry string for replacing
   * @return new string after replacing
   */
  @NotNull static String replaceXmlHeaderVersion(@NotNull final String xmlEntry){
    final Matcher matcher = XML_HEADER_PATTERN.matcher(xmlEntry);
    if (matcher.find()) {
      return matcher.replaceFirst(matcher.group(1) + "1.1" + matcher.group(3));
    }
    return xmlEntry;
  }
  /**
   * Replace control charecrters in string and return new string.
   * @param xmlEntry string for replacing
   * @return new string after replacing
   */
  @NotNull
  private static String replaceControlChars(@NotNull final String xmlEntry) {
    final StringBuilder tempEntry = new StringBuilder();
    for (int i = 0; i < xmlEntry.length(); i++){
      if ((xmlEntry.charAt(i) >= 0x0000 && xmlEntry.charAt(i) <= 0x001f ||
          xmlEntry.charAt(i) >= 0xc2a0 && xmlEntry.charAt(i) <= 0x001f) &&
          xmlEntry.charAt(i) != 0x000a && xmlEntry.charAt(i) != 0x0009 && xmlEntry.charAt(i) != 0x000d &&
          xmlEntry.charAt(i) != 0x0085 && xmlEntry.charAt(i) != 0x2028) {
          tempEntry.append("&#x").append(Integer.toHexString(xmlEntry.charAt(i))).append(';');

      }
      else{
        tempEntry.append(xmlEntry.charAt(i));
      }
    }
    return tempEntry.toString();
  }

  /**
   * Parses Xml file entry and public test results.
   * @param xmlEntry Xml file entry
   * @param testSuiteName name of test suite
   */
  private void parseXmlTestResults(@NotNull final String xmlEntry, @NotNull final String testSuiteName){
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
   * @param xmlEntry File with Xml
   * @return string with update xml data
   */
  @NotNull
  private static String replaceAmp(@NotNull final String xmlEntry) {
    return xmlEntry.replaceAll("&[^;]", "&#x26;");
  }
}
