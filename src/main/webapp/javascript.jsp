<%@ page contentType="text/javascript;charset=UTF-8" %> 
<%@ taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>

<%request.setCharacterEncoding("UTF-8");%>
<%response.setCharacterEncoding("UTF-8");%>


var theElements = [
  <logic:iterate id="site" name="sites">
     ["<jsp:getProperty name="site" property="displayName" />", "<jsp:getProperty name="site" property="name" />"],
  </logic:iterate>
 ];

var theHints = [

<logic:present name="cookieList" scope="request">
<logic:iterate id="site" name="cookieList">
  <logic:present name="entityID" scope="request">

    <bean:define id="returnIDParam" name="returnIDParam"/>
    <bean:define id="ei" name="entityID" />
    <bean:define id="re" name="returnX"/>

    [ 
      "<bean:write name="requestURL" />?entityID=<%= java.net.URLEncoder.encode(ei.toString(), "utf-8") %>&return=<%= java.net.URLEncoder.encode(re.toString(), "utf-8") %>&returnIDxParam=<%= java.net.URLEncoder.encode( returnIDParam.toString(), "utf-8" ) %>&cache=perm&action=selection&origin=<jsp:getProperty name="site" property="name" />"
      ,
      "<jsp:getProperty name="site" property="displayName" />"
    ],
  </logic:present>
  <logic:notPresent name="entityID" scope="request">
    <bean:define id="targ" name="target" />
    <bean:define id="shire" name="shire" />
    <bean:define id="pid" name="providerId" />

    [
      "<bean:write name="requestURL" />?target=<%= java.net.URLEncoder.encode(targ.toString(),"utf-8") %>&shire=<%= java.net.URLEncoder.encode(shire.toString(),"utf-8") %>&providerId=<%= java.net.URLEncoder.encode(pid.toString(),"utf-8") %>&time=<bean:write name="time" />&cache=perm&action=selection&origin=<jsp:getProperty name="site" property="name" />"
      ,
      "<jsp:getProperty name="site" property="displayName" />"
    ],
  </logic:notPresent>
</logic:iterate>
</logic:present>
];
  