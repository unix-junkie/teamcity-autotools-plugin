<%@ page import="jetbrains.buildServer.log.Loggers" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="bean" class="jetbrains.buildServer.autotools.server.AutotoolsBuildBean"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>




<%
  Loggers.SERVER.info("XXX: begin" + bean.getConfigurePath() + " " + bean.getConfigurePath());
%>
<forms:workingDirectory/>

<tr class="parameter">
  <td>Additional ./configure command line parameters:</td>
  <td><props:textProperty name="${bean.additionalConfigurateParamsKey}" className="longField" maxlength="256"/>
    <span class="smallNote">Enter parametors for ./configure (equals '--config &lt;tgt&gt;' cmd param)
    </span>
  </td>
</tr>


<tr class="parameter">
  <td>Additional make command line parameters:</td>
  <td><props:textProperty name="${bean.additionalMakeParamsKey}" className="longField" maxlength="256"/>
    <span class="smallNote">Enter parametors for make (equals '--config &lt;tgt&gt;' cmd param)
    </span>
  </td>
</tr>

<tr class="parameter">
<td>
  <props:checkboxProperty name="${bean.needAutoreconf}" />
  <label for="${bean.needAutoreconf}">Execute autoreconf</label>
  <br/>
</td>
</tr>


