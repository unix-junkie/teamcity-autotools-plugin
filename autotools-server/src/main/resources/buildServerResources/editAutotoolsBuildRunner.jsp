<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="bean" class="jetbrains.buildServer.autotools.server.AutotoolsBuildBean"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>


<style type="text/css">
  div.smallWidth > span.smallNote {
    max-width: 33em;
  }

  span.code {
    font-family: Monospace, Courier New, Courier, monospace;
    background-color: inherit;
    color: inherit;
    font-size: 100%;
  }
</style>


<forms:workingDirectory/>
<tr class="groupingTitle ">
  <td colspan="2">Configure</td>
</tr>

<tr>
  <th>Pre-configure:</th>
  <td>
    <props:checkboxProperty name="${bean.needAutoreconf}"/>
    <label for="${bean.needAutoreconf}">Execute <span class="code">autoreconf</span>
      before running <span class="code">configure</span></label>
    <div class="smallWidth">
      <span class="smallNote">Leave checked to (re-)generate the
        <span class="code">configure</span> script if one is missing.</span>
    </div>
  </td>
</tr>
<tr class="advancedSetting">
  <th>Directory containing the configure script:</th>
  <td>
    <props:textProperty
        name="${bean.sourcePath}"
        value="${propertiesBean.defaultProperties[bean.sourcePath]}"
        className="longField"
        maxlength="256"/>
    <div class="smallWidth">
      <span class="smallNote">Specify the directory containing the
        <span class="code">configure</span> script. Leave blank to use the
        defaults.</span>
    </div>
  </td>
</tr>
<tr>
  <th>Extra configure parameters:</th>
  <td>
    <props:textProperty
        name="${bean.additionalConfigurateParamsKey}"
        value="${propertiesBean.defaultProperties[bean.additionalConfigurateParamsKey]}"
        className="longField"
        maxlength="256"/>
    <div class="smallWidth">
      <span class="smallNote">Specify additional command line parameters for the
        <span class="code">configure</span> script
        (e.g.&nbsp;<span class="code">--sysconfdir=/etc --with-gnu-ld</span>).
        Leave blank to use the defaults.
      </span>
    </div>
  </td>
</tr>
<tr class="groupingTitle ">
  <td colspan="2">Build</td>
</tr>
<tr>
  <th>Build-time make targets:</th>
  <td><props:textProperty
      name="${bean.additionalMakeParamsKey}"
      value="${propertiesBean.defaultProperties[bean.additionalMakeParamsKey]}"
      className="longField"
      maxlength="256"/>
    <div class="smallWidth">
      <span class="smallNote">Specify <span class="code">make</span> targets and
        <span class="code">$(VARIABLES)</span> to build your code. Leave blank
        to use the defaults.</span>
    </div>
  </td>
</tr>
<tr class="groupingTitle">
  <td colspan="2">Test</td>
</tr>
<tr>
  <th>Test-time make targets:</th>
  <td><props:textProperty
      name="${bean.makeCheckParam}"
      value="${propertiesBean.defaultProperties[bean.makeCheckParam]}"
      className="longField"
      maxlength="256"/>
    <div class="smallWidth">
      <span class="smallNote">Specify <span class="code">make</span> targets and
        <span class="code">$(VARIABLES)</span> to test your code. Leave blank to
        use the defaults.
      </span>
    </div>
  </td>
</tr>
<tr class="advancedSetting">
  <th rowspan="2">
    DejaGnu options:
  </th>
  <td>
    <props:checkboxProperty
        name="${bean.needDejagnuXmlReplaceAmp}"
        value="${propertiesBean.defaultProperties[bean.needDejagnuXmlReplaceAmp]}" />
    <label for="${bean.needDejagnuXmlReplaceAmp}">Escape the <b>&amp;</b>
      character in DejaGnu XML test reports</label>
  </td>
</tr>

<tr class="advancedSetting">
  <td>
    <props:checkboxProperty
        name="${bean.needDejagnuXmlReplaceControls}"
        value="${propertiesBean.defaultProperties[bean.needDejagnuXmlReplaceAmp]}" />
    <label for="${bean.needDejagnuXmlReplaceControls}">Replace control
      characters in DejaGnu XML test reports</label>
    <div class="smallWidth">
      <span class="smallNote">Some DejaGnu versions produce malformed XML. Leave
        both boxes checked if not sure.</span>
    </div>
  </td>
</tr>
