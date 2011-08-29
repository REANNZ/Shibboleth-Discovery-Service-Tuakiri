<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=UTF-8" %>
<html>

<%@ taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
 
<head>
    <link rel="stylesheet" title="normal" type="text/css"
    href="wayf.css" /> 

    <title>Reset permanent redirection to your IdP</title>
</head>
<body>

    <div class="head">

<h2>Reset permanent redirection to your IdP</h2>

</div>

<div class="selector">
    <p class="text">

<%@ page import="edu.internet2.middleware.shibboleth.wayf.plugins.provider.SamlCookiePlugin" %>
<%!
    private static Cookie getCookie(HttpServletRequest req, String cookieName) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(cookieName)) {
                    return cookies[i];
                }
            }
        }
        return null;
    }
%>

<%!
   private static String RESET_PARAM_NAME = "reset";
%>

<%
   String resetParam = request.getParameter(RESET_PARAM_NAME);

   if (resetParam != null && !resetParam.equals("")) {

%>

<% /* delete cookies named SamlCookiePlugin.COOKIE_NAME and SamlCookiePlugin.REDIRECT_COOKIE_NAME */
        Cookie cookie = getCookie(request, SamlCookiePlugin.COOKIE_NAME);

        if (cookie != null) {
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }

        /* also delete the redirect cookie */
        Cookie redirectCookie = getCookie(request, SamlCookiePlugin.REDIRECT_COOKIE_NAME);
        if (redirectCookie != null) {
            redirectCookie.setPath("/");
            redirectCookie.setMaxAge(0);
            response.addCookie(redirectCookie);
        }
%>
<strong>Your permanent organisation selection has been successfully removed.</strong>

<%
} else {
%>
You can clear your permanent organisation selection here.
    <form method="get" name="ClearForm" action="<%=request.getRequestURL()%>" onSubmit="return !ClearIdPselection();" >
        <input type="hidden" name="reset" value="reset" />
        <input type="submit" name="ClearSubmit" value="Reset organisation selection" />
    </form>

<%
} /* else - reset param */
%>
    </div>

    <div class="footer">
        <p class="text">
<!--CONFIG-->
        </p>
    </div>

</body>

