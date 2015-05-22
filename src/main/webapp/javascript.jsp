<%@ page contentType="text/javascript;charset=UTF-8" %> 
<%@ taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tlds/esapi.tld" prefix="esapi" %>

<%request.setCharacterEncoding("UTF-8");%>
<%response.setCharacterEncoding("UTF-8");%>

<%
Object requestURL = request.getAttribute("requestURL");

Object shire = request.getAttribute("shire");
Object target = request.getAttribute("target");
Object providerId = request.getAttribute("providerId");
Object time = request.getAttribute("time");

Object entityID = request.getAttribute("entityID");
Object returnX =  request.getAttribute("returnX");
Object returnIDParam = request.getAttribute("returnIDParam");
%>

var theElements = [
  <logic:iterate id="site" name="sites">
     ["<esapi:encodeForJavaScript><jsp:getProperty name="site" property="displayName" /></esapi:encodeForJavaScript>", "<esapi:encodeForJavaScript><jsp:getProperty name="site" property="name" /></esapi:encodeForJavaScript>"],
  </logic:iterate>
 ];

var theHints = [

<logic:present name="cookieList" scope="request">
<logic:iterate id="site" name="cookieList">
  <logic:present name="entityID" scope="request">

    [ 
      "<esapi:encodeForJavaScript><%=requestURL%></esapi:encodeForJavaScript>?entityID=<esapi:encodeForJavaScript><%=entityID%></esapi:encodeForJavaScript>&return=<esapi:encodeForJavaScript><%=returnX%></esapi:encodeForJavaScript>&returnIDxParam=<esapi:encodeForJavaScript><%=returnIDParam%></esapi:encodeForJavaScript>&cache=perm&action=selection&origin=<esapi:encodeForJavaScript><jsp:getProperty name="site" property="name" /></esapi:encodeForJavaScript>"
      ,
      "<esapi:encodeForJavaScript><jsp:getProperty name="site" property="displayName" /></esapi:encodeForJavaScript>"
    ],
  </logic:present>
  <logic:notPresent name="entityID" scope="request">

    [
      "<esapi:encodeForJavaScript><%=requestURL%></esapi:encodeForJavaScript>?target=<esapi:encodeForJavaScript><%=target%></esapi:encodeForJavaScript>&shire=<esapi:encodeForJavaScript><%=shire%></esapi:encodeForJavaScript>&providerId=<esapi:encodeForJavaScript><%=providerId%></esapi:encodeForJavaScript>&time=<esapi:encodeForJavaScript><%=time%></esapi:encodeForJavaScript>&cache=perm&action=selection&origin=<esapi:encodeForJavaScript><jsp:getProperty name="site" property="name" /></esapi:encodeForJavaScript>"
      ,
      "<esapi:encodeForJavaScript><jsp:getProperty name="site" property="displayName" /></esapi:encodeForJavaScript>"
    ],
  </logic:notPresent>
</logic:iterate>
</logic:present>
];

