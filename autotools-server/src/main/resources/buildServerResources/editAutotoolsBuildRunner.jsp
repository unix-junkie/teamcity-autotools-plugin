<%@ page import="jetbrains.buildServer.log.Loggers" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="bean" class="jetbrains.buildServer.autotools.server.AutotoolsBuildBean"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>


<forms:workingDirectory/>
<tr class="groupingTitle ">
  <td colspan="2">Configure</td>
</tr>

<tr>
  <td/>
  <td>
    <props:checkboxProperty name="${bean.needAutoreconf}"/>
    <label for="${bean.needAutoreconf}">Execute autoreconf before running configure</label>
    <br/>
    <span class="smallNote"><div style="max-width: 33em">Leave checked to (re-)generate the configure script if one is missing.</div>
      </span>
  </td>
</tr>
<tr class="advancedSetting">
  <th>Directory containing the configure script:</th>
  <td><props:textProperty name="${bean.sourcePath}" value="${propertiesBean.defaultProperties[bean.sourcePath]}" className="longField" maxlength="256"/>
    <span class="smallNote"><div style="max-width: 33em">Specify the directory containing the configure script. Leave blank to use the defaults.</div>
      </span>
  </td>
</tr>
<tr>
  <th>Extra configure parameters:</th>
  <td><props:textProperty name="${bean.additionalConfigurateParamsKey}"  value="${propertiesBean.defaultProperties[bean.additionalConfigurateParamsKey]}"  className="longField" maxlength="256"/>
    <span class="smallNote"><div style="max-width: 33em">Specify additional command line parameters for the configure script
      (e.g.:<code><b> --sysconfdir=/etc --with-gnu-ld</b></code>). Leave blank to use the defaults.</div>
      </span>
  </td>
</tr>
<tr class="groupingTitle ">
  <td colspan="2">Build</td>
</tr>
<tr>
  <th>Build time make targets and VARIABLES:</th>
  <td><props:textProperty name="${bean.additionalMakeParamsKey}" value="${propertiesBean.defaultProperties[bean.additionalMakeParamsKey]}"  className="longField" maxlength="256"/>
    <span class="smallNote"><div style="max-width: 33em">Specify additional make targets and VARIABLES to build your code. Leave blank to use the defaults.</div>
      </span>
  </td>
</tr>
<tr class="groupingTitle">
  <td colspan="2">Test</td>
</tr>
<tr>
  <th>Test time make check targets:</th>
  <td><props:textProperty name="${bean.makeCheckParam}" value="${propertiesBean.defaultProperties[bean.makeCheckParam]}" className="longField" maxlength="256"/>
    <span class="smallNote"><div style="max-width: 33em">Specify make targets and VARIABLES to test your code.<div>
    </span>
  </td>
</tr>
<tr class="advancedSetting">
  <td/>
  <td>
    <props:checkboxProperty name="${bean.needDejagnuXmlReplaceAmp}" value="${propertiesBean.defaultProperties[bean.needDejagnuXmlReplaceAmp]}" />
    <label for="${bean.needDejagnuXmlReplaceAmp}">Escape <b>&</b> in DejaGnu XML test result files</label>
    <br/>
    <span class="smallNote"><div style="max-width: 33em">Some DejaGnu versions produce malformed XML. Leave checked if not sure.</div>
      </span>
  </td>
</tr>

<tr class="advancedSetting">
  <td/>
  <td>
    <props:checkboxProperty name="${bean.needDejagnuXmlReplaceControls}" value="${propertiesBean.defaultProperties[bean.needDejagnuXmlReplaceAmp]}" />
    <label for="${bean.needDejagnuXmlReplaceControls}">Replace control characters in DejaGnu XML test result files</label>
    <span class="smallNote"><div style="max-width: 33em">Some DejaGnu versions produce malformed XML. Leave checked if not sure.</div>
      </span>
  </td>
</tr>