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
	<!-- <link rel="stylesheet" type="text/css" href="main.css" /> -->
	<link rel="stylesheet" type="text/css" href="wayf.css" />
	<title>Access System Failure</title>
</head>

<body>

<div id="content">

<div id="background_a">
<div id="background_b">
<div id="reset_background_c">

<div class="error_head">
<!-- <img src="images/logo.jpg" alt="Shibboleth" /> -->
<div class="logo"><img src="/static/images/tuakiri.png" alt="Tuakiri" /></div>
<div class="NZAF_title"><h1>New Zealand Access Federation</h1></div>
<ul class="ds_links">
	<li><a target="_blank" href="https://www.tuakiri.ac.nz">Home</a></li>
	<li><a target="_blank" href="https://tuakiri.ac.nz/confluence/display/Tuakiri/About+Us">About</a></li>
	<li><a target="_blank" href="https://tuakiri.ac.nz/confluence/display/Tuakiri/Support+Desk">Support</a></li>
</ul>
<h2>Discovery Service Failure</h2>
</div>

<div class="body">

<p>The inter-institutional access system experienced a technical failure.</p>

<p>Please email <a href="mailto:support@tuakiri.ac.nz">Tuakiri support</a> and include the following error message:</p>

<logic:notEmpty name="requestURL">
<p class="error">Discovery Service failure at (<esapi:encodeForHTML><%=requestURL%></esapi:encodeForHTML>)</p>

<p><%=errorText%></p>
</logic:notEmpty>
<logic:empty name="requestURL">
<p class="error">The Discovery Service should not be called directly</p>
</logic:empty>
</div>

</div> <!-- end of background divs -->
</div>
</div>

</div>

<!-- display DS version -->
<logic:present name="dsVersion" scope="request">
<!-- DS version: <esapi:encodeForHTML><%= (String)request.getAttribute("dsVersion") %></esapi:encodeForHTML> -->
</logic:present>
<!-- Internal Hostname: <esapi:encodeForHTML><%= (String)request.getAttribute("internalHostname") %></esapi:encodeForHTML> -->

</body>
</html>
