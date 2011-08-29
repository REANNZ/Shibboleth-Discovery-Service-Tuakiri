<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
 
<html>

<head>
    <link rel="stylesheet" title="normal" type="text/css"
    href="wayf.css" /> <title>Reset permanent redirection to your IdP</title>
    </head>
<body>

    <div class="head">
        <div class="logo"><img src="images/tuakiri.png" alt="Tuakiri New Zealand Federation" /></div>
<!-- Tuakiri links -->
<a href="http://tuakiri.ac.nz/confluence/display/Tuakiri/Home">About Tuakiri</a> &nbsp; <a href="http://tuakiri.ac.nz/confluence/display/Tuakiri/Support+Desk">Help</a>
        <h1>Reset permanent redirection to your IdP</h1>
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
<strong>Your permanent IdP selection has been successfully removed</strong>
    </div>

    <div class="footer">
        <p class="text">
<!--CONFIG-->
        </p>
    </div>
</body>

