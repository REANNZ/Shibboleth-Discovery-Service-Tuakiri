<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html 
	PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
	"DTD/xhtml1-strict.dtd">
	<%@ taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic" %>
	<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
	<%@ taglib uri="/WEB-INF/tlds/esapi.tld" prefix="esapi" %>
	

<%
Object requestURL = request.getAttribute("requestURL");
Object errorText = request.getAttribute("errorText");
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<link rel="stylesheet" type="text/css" href="main.css" />
	<title>Access System Failure</title>
</head>

<body>
<div class="head">
<img src="images/logo.jpg" alt="Logo" />
<h1>Inter-institutional Access System Failure</h1>
</div>

<p>The inter-institutional access system experienced a technical failure.</p>

<p>Please email <a href="mailto:user@domain"> administrator's name</a> and include the following error message:</p>

<logic:notEmpty name="requestURL">
<p class="error">Discovery Service failure at (<esapi:encodeForHTML><%=requestURL%></esapi:encodeForHTML>)</p>

<p><%=errorText%></p>
</logic:notEmpty>
<logic:empty name="requestURL">
<p class="error">The Discovery Service should not be called directly</p>
</logic:empty>


<!-- display DS version -->
<logic:present name="dsVersion" scope="request">
<!-- DS version: <esapi:encodeForHTML><%= (String)request.getAttribute("dsVersion") %></esapi:encodeForHTML> -->
</logic:present>
<!-- Internal Hostname: <esapi:encodeForHTML><%= (String)request.getAttribute("internalHostname") %></esapi:encodeForHTML> -->

</body>
</html>
