<%@ page contentType="text/plain" %><%

boolean is_OK = true;

String statusControlFileName = getServletContext().getInitParameter("StatusControlFile");

if (statusControlFileName != null ) {
    java.io.File statusControlFile = new java.io.File(statusControlFileName);
    if (statusControlFile.exists() ) is_OK = false;
};

if (!is_OK) response.setStatus(404);

%><%= is_OK ? "ok" : "DOWN" %><%

/*

IMPORTANT: Notes on configuration needed to actiate this functionality: 

This code would only be checking for the presence of a "disable" control file
if the name is provided as the StatusControlFile Context parameter.

A Context parameter can be provided either in the web-app element of the
web.xml deployment descriptor or in the Context descriptor - so either

<web-app>
        <context-param><param-name>StatusControlFile</param-name><param-value>/var/www/html/disable_server</param-value></context-param>
</web-app>

OR in /etc/tomcat?/Catalina/localhost/ds.xml inside the Context element as 

<Context ...>
  ...
    <Parameter name="StatusControlFile" value="/var/www/html/disable_server" />

</Context>

References:
* https://tomcat.apache.org/tomcat-5.5-doc/config/context.html#Context_Parameters
* http://tomcat.apache.org/tomcat-5.5-doc/appdev/web.xml.txt

*/

%>
