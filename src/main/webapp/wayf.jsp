<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=UTF-8" %> 
<html>

<%@ taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>

<%request.setCharacterEncoding("UTF-8");%>
<%response.setCharacterEncoding("UTF-8");%>

<logic:present name="showComments" scope="Request">

<!-- TO CONFIGURE THIS FOR A SPECIFIC SITE
     =====================================

     Before you deploy this jsp you need to look for CONFIG below.
     These mark places where you should make changes. 

     If you want to make more profound changes but only to the jsp,
     you should read the sections marked PROGRAMMING NOTE below.-->

<!-- PROGRAMMING NOTE

     "requestURL" contains the URL that was specified to get the
     WAYF going.  The jsp uses it mostly for submitting result back to
     the WAYF and error reporting -->

</logic:present>

    <jsp:useBean id="requestURL" scope="request" class="java.lang.String"/>

<logic:present name="showComments" scope="Request">

<!-- PROGRAMMING NOTE

     shire, target, provider and time are all part of the Shibboleth
     1.3 discovery protocol and need to be specified as parameters to the WAYF

     entityID, return, returnIDParam are all part of the
     SAML Discovery protocol.


-->
</logic:present>

<logic:notPresent name="entityID" scope="request">
<logic:notPresent name="shire" scope="request">

<jsp:forward page = "wayferror.jsp"/>
</logic:notPresent>
</logic:notPresent>


<logic:present name="showComments" scope="Request">

<!-- PROGRAMMING NOTE
     In addition to the above.  The WAYF may also supply the following to
     the jsp.

     "cookieList" If this exists it represents the contents of the
         _saml_idp cookie (possibly filtered to remove IdPs which
         cannot serve the SP).  It is a Collection of IdPSite objects,
         which themselves have the following properties:

       "name" The uri for the IdP, which needs to be returned to the
              WAYF in the "origin" parameter.

       "displayName" User friendly name (taken from its alias)

       "addressFor" The (ungarnished) URL for the IdP. This could be
              used to create a direct hyperlink to the IdP

     "sites" If this exists it contains all the possible IdPs for for
         the SP (possibly filtered).  It is a Collection of IdPSite
         Objects which are described above.  This is only present if
         provideList was defined true in the configuration.

     "siteLists" If this exists it contains all the possible metadata
         files which can service for the SP (possibly filtered).  It
         is a collection of IdPSiteSetEntry Objects which have two
         properties:

         "name" This is the displayName from the Metadata element in
            the WAYF configuration file

         "sites" This represents the IdPs.  Again it is a collection
            of IdPSite Objects

         It is only present if provideListOfList was defined true in
         the configuration.

     "singleSiteList" if this is present, then there is only one
         IdPSiteSetEntry Object in "siteLists".

     "searchresultempty" If this is present then it means that a
         search was performed, but no suitable IdPs were returned.

     "searchresults" If this is present it represents the list of IdPs
         which matched a previous search.  It is a Collection of
         IdPSite Objects. -->

<!-- PROGRAMMING NOTE

     The jsp communicates back to the WAYF via the parameters listed
     above, and:

        "action" what the WAYF has to do.  Possible contents are:

            "lookup" - refresh the screen.
            "search" - perform a search on the contents parameter "string"
            "selection" - redirect to the IdP with the uri "origin"

        "cache" preserve any selection in the _saml_idp cookie. A
            value of "session" makes the cookie last for the browser
            session, "perm" gives it the lifetime specified in the
            configuration file.  

      The tabindex is hard wired.  Fortunately the standard allows us to
      have duplicate numbers and says the order is the order things
      get emitted.  We use these numbers

      10 - Recently used sites hyperlinks 
      20 - <clear button for above> 
      25 - AutoSuggestion
      30 Federation selection 
      40 IdP within Selection 
      50 Select button 
      60 How long to remember selector 
      70 Search entry 
      80 Search Button 
      90 List of search results
      100 Select search result
      110 How long to remember search results
      120 Hyperlink to admin user. 

-->

</logic:present>

<head>
    <link rel="stylesheet" title="normal" type="text/css"
    href="wayf.css" /> <title>Identity Provider Selection</title>
    </head>

<body>
    <div class="head">
        <h1>

Select an identity provider

        </h1>
    </div>

    <div class="selector">
    <p class="text">

<!--CONFIG-->

The Service you are trying to reach requires that you
authenticate with your home organization, enter the name below.

    </p>
    <logic:present name="cookieList" scope="request">

        <h2>

Recently used organizations:

        </h2>   

<logic:present name="showComments" scope="Request">

<!-- PROGRAMMING NOTE
 
     Generate a hyperlink back to the WAYF.  Note that we are
     simulating the user having specified a permanent cookie -->

</logic:present>

        <logic:iterate id="site" name="cookieList">
            <p  class="text">
              <logic:present name="entityID" scope="request">

                <bean:define id="returnIDParam" name="returnIDParam"/>
                <bean:define id="ei" name="entityID" />
                <bean:define id="re" name="returnX"/>

                 <a tabindex="10" href="<bean:write name="requestURL" />?entityID=<%= java.net.URLEncoder.encode(ei.toString(), "utf-8") %>&return=<%= java.net.URLEncoder.encode(re.toString(), "utf-8") %>&returnIDxParam=<%= java.net.URLEncoder.encode( returnIDParam.toString(), "utf-8" ) %>&cache=perm&action=selection&origin=<jsp:getProperty name="site" property="name" />">
                    <jsp:getProperty name="site" property="displayName" />
                </a>
              </logic:present>
              <logic:notPresent name="entityID" scope="request">
                <bean:define id="targ" name="target" />
                <bean:define id="shire" name="shire" />
                <bean:define id="pid" name="providerId" />
                <a tabindex="10" href="<bean:write name="requestURL" />?target=<%= java.net.URLEncoder.encode(targ.toString(),"utf-8") %>&shire=<%= java.net.URLEncoder.encode(shire.toString(),"utf-8") %>&providerId=<%= java.net.URLEncoder.encode(pid.toString(),"utf-8") %>&time=<bean:write name="time" />&cache=perm&action=selection&origin=<jsp:getProperty name="site" property="name" />">
                    <jsp:getProperty name="site"
                    property="displayName" />
                </a>
              </logic:notPresent>
            </p>
        </logic:iterate>

<logic:present name="showComments" scope="Request">

<!-- PROGRAMMING NOTE

     We defined the ClearCache.Wayf service in wayfconfig.  So we know
     it is here.  This will empty the cookie and loop -->

</logic:present>

        <form method="get" action="ClearCache.wayf" >
          <div>
          <logic:notPresent name="entityID" scope="request">
            <input type="hidden" name="shire" value="<bean:write name="shire" />" />
            <input type="hidden" name="target" value="<bean:write name="target" />" />
            <input type="hidden" name="providerId" value="<bean:write name="providerId" />" />
            <logic:present name="time" scope="request">
               <input type="hidden" name="time" value="<bean:write name="time" />" />
            </logic:present>
          </logic:notPresent>
          <logic:present name="entityID" scope="request">
            <input type="hidden" name="entityID" value="<bean:write name="entityID" />" />
            <input type="hidden" name="returnX" value="<bean:write name="returnX" />" />
            <input type="hidden" name="returnIDParam" value="<bean:write name="returnIDParam" />" />
          </logic:present>
          <input tabindex="20" type="submit" value="Clear" />
          </div>
        </form>

  </logic:present> <!-- Previous Selections -->

<logic:present name="showComments" scope="Request">

<!-- PROGRAMMING NOTE
 
   Add the "instant search" dialogue.

</logic:present>

    <div class="list">
       <logic:present name="sites" scope="request">
              <h2>
              Enter institution name:
              </h2>
              <form autocomplete="OFF" action="">
                <div>
                  <logic:notPresent name="entityID" scope="request">
                    <input type="hidden" name="shire" value="<bean:write name="shire" />" />
                    <input type="hidden" name="target" value="<bean:write name="target" />" />
                    <input type="hidden" name="providerId" value="<bean:write name="providerId" />" />
                    <logic:present name="time" scope="request">
                       <input type="hidden" name="time" value="<bean:write name="time" />" />
                    </logic:present>
                  </logic:notPresent>
                  <logic:present name="entityID" scope="request">
                    <input type="hidden" name="entityID" value="<bean:write name="entityID" />" />
                    <input type="hidden" name="returnX" value="<bean:write name="returnX" />" />
                    <input type="hidden" name="returnIDParam" value="<bean:write name="returnIDParam" />" />
                  </logic:present>
                  <input type="hidden" id="enterOrigin" name="origin" value="unspec" />
                  <input type="hidden" id="enterType"   name="action" value="search" />
                  <input type="text"   id="enterText"   name="string" value="" tabindex="25" size="54"/>
                  <input type="submit" id="enterSubmit"               value="Search"/>
                  <input type="hidden"                  name="cache"  value="perm"/>
                </div>
              </form>
        </logic:present>   
     
        <h2>

<logic:present name="showComments" scope="Request">

Provide a static drop down or a dynamically republished one. - you may wish to remove this code

</logic:present>

Or choose from a list:

        </h2>

        <logic:present name="sites" scope="request">
        <logic:notPresent name="siteLists" scope="request">

            <form method="get" action="<bean:write name="requestURL" />">
              <div>
                <logic:notPresent name="entityID" scope="request">
                    <input type="hidden" name="shire" value="<bean:write name="shire" />" />
                    <input type="hidden" name="target" value="<bean:write name="target" />" />
                    <input type="hidden" name="providerId" value="<bean:write name="providerId" />" />
                    <logic:present name="time" scope="request">
                         <input type="hidden" name="time" value="<bean:write name="time" />" />
                    </logic:present>
                </logic:notPresent>
                <logic:present name="entityID" scope="request">
                    <input type="hidden" name="entityID" value="<bean:write name="entityID" />" />
                    <input type="hidden" name="returnX" value="<bean:write name="returnX" />" />
                    <input type="hidden" name="returnIDParam" value="<bean:write name="returnIDParam" />" />
                 </logic:present>
                 <input type="hidden" name="action" value="selection" />
                 <select name="origin" id="hackForie6" tabindex="40">      
                     <logic:iterate id="site" name="sites">
                         <option value="<jsp:getProperty name="site" property="name" />">
                             <jsp:getProperty name="site" property="displayName" />
                         </option>
                     </logic:iterate>
                 </select>
                 <input type="submit" value="Select" tabindex="50" />
                 <select name="cache" tabindex="60">
                     <option value="false"> Do not remember</option>
                     <option value="session" selected="selected"> Remember for session</option>
                     <option value="perm"> Remember for a week</option>
                 </select>
              </div>
            </form>
        </logic:notPresent>
        </logic:present>

<logic:present name="showComments" scope="Request">

<!-- PROGRAMMING NOTE
     Build two tables side by side, one with the Federation names and 'ALL' (if apposite)
     and the other will be dynamically populated with the members of that federation.

     This needs to work in the face of no javascript, so we initially populate the 
     Right hand list with all the IdPs.  The first Selection in the Left hand Table will
     shrink this list

     The 'lists of all IdPs' is derived from the one which java gives us (if it did)
     otherwise it is derived by a double iteration through the List of Lists.  This
     makes for complicated looking code, but it's dead simple really.

 -->

</logic:present>

        <logic:present name="siteLists" scope="request">
          <form method="get" action="<bean:write name="requestURL" />">
            <div>
             <logic:notPresent name="entityID" scope="request">
                 <input type="hidden" name="shire" value="<bean:write name="shire" />" />
                 <input type="hidden" name="target" value="<bean:write name="target" />" />
                 <input type="hidden" name="providerId" value="<bean:write name="providerId" />" />
                 <logic:present name="time" scope="request">
                    <input type="hidden" name="time" value="<bean:write name="time" />" />
                 </logic:present>
             </logic:notPresent>
             <logic:present name="entityID" scope="request">
                 <input type="hidden" name="entityID" value="<bean:write name="entityID" />" />
                 <input type="hidden" name="returnX" value="<bean:write name="returnX" />" />
                 <input type="hidden" name="returnIDParam" value="<bean:write name="returnIDParam" />" />
              </logic:present>
             <table id="tab">
               <tr>
                <th>Federation </th>
                <th>organization</th>
               </tr>
               <tr><td>
                 <select name="FedSelector" size="10" id="FedSelect" tabindex="30" 
                             onchange="changedFed(this.form.origin,
                                                  this.form.FedSelector[this.form.FedSelector.selectedIndex].value);">
                   <logic:iterate id="siteset" name="siteLists">
                     <logic:present name="singleSiteList" scope="request">

                       <!-- Only One site so select it -->

                       <option value="<jsp:getProperty name="siteset" property="name"/>" selected="selected">
                         <jsp:getProperty name="siteset" property="name"/>
                       </option>
                     </logic:present>
                     <logic:notPresent name="singleSiteList" scope="request">
                       <option value="<jsp:getProperty name="siteset" property="name"/>">
                         <jsp:getProperty name="siteset" property="name"/>
                       </option>
                     </logic:notPresent>
                   </logic:iterate>
                   <logic:notPresent name="singleSiteList" scope="request">

                     <!-- More than one site so select the 'All' -->

                     <option value="ALL" selected="selected">
                         All Sites
                     </option>
                   </logic:notPresent>
                 </select></td><td>
                 <input type="hidden" name="action" value="selection" />
                 <select name="origin" size="10" id="originIdp" tabindex="40"> 
                   <logic:present name="sites" scope="request">
                     <logic:iterate id="site" name="sites">
                       <option value="<jsp:getProperty name="site" property="name" />">
                         <jsp:getProperty name="site" property="displayName" />
                       </option>
                     </logic:iterate>
                   </logic:present>

                   <logic:notPresent name="sites" scope="request">
                     <logic:iterate id="siteset" name="siteLists">
                       <logic:iterate id="site" name="siteset" property="sites">
                         <option value="<jsp:getProperty name="site" property="name" />">
                           <jsp:getProperty name="site" property="displayName" />
                         </option>
                       </logic:iterate>
                     </logic:iterate>        
                   </logic:notPresent>
                 </select>
               </td></tr>
             </table>
             <p>
               <input type="submit" value="Select" tabindex="50"  />
               <select name="cache" tabindex="60" >
                 <option value="false"> Do not remember</option>
                 <option value="session" selected="selected"> Remember for session</option>
                 <option value="perm"> Remember for a week</option>
               </select>
             </p>
            </div>
           </form>
        </logic:present>
        </div>


        <div class="search">

<logic:present name="showComments" scope="Request">

<!-- This is here for completeness - it shows the "old fashioned way" to do search -->

            <span class="option">or</span>

            <h2>

Search by keyword:

            </h2>

            <form method="get" action="<bean:write name="requestURL" />">
              <div>
                <p>

                <logic:notPresent name="entityID" scope="request">
                    <input type="hidden" name="shire" value="<bean:write name="shire" />" />
                    <input type="hidden" name="target" value="<bean:write name="target" />" />
                    <input type="hidden" name="providerId" value="<bean:write name="providerId" />" />
                    <logic:present name="time" scope="request">
                         <input type="hidden" name="time" value="<bean:write name="time" />" />
                    </logic:present>
                </logic:notPresent>
                <logic:present name="entityID" scope="request">
                    <input type="hidden" name="entityID" value="<bean:write name="entityID" />" />
                    <input type="hidden" name="returnX" value="<bean:write name="returnX" />" />
                    <input type="hidden" name="returnIDParam" value="<bean:write name="returnIDParam" />" />
                 </logic:present>

                    <input type="hidden" name="action" value="search" />
                    <input type="text" name="string" tabindex="70" />
                    <input type="submit" value="Search" tabindex="80" />
                </p>
              </div>
            </form>

<!-- The end of the old code.  Below is where search results go -->

</logic:present>
            <logic:present name="searchResultsEmpty" scope="request">
                <p class="error">

No provider was found that matches your search criteria, please try again.

                </p>
            </logic:present>

            <logic:present name="searchresults" scope="request">
                <h3>

Search results:

                </h3>
                <form method="get" action="<bean:write name="requestURL" />">
                  <div>
                    <ul>
                        <logic:iterate id="currResult" name="searchresults">
                            <li>
                                <input type="radio" name="origin" tabindex="90" value="<jsp:getProperty name="currResult" property="name" />" />
                                <jsp:getProperty name="currResult" property="displayName" />
                            </li>
                        </logic:iterate>
                    </ul>
                    <p>
                  <logic:notPresent name="entityID" scope="request">
                      <input type="hidden" name="shire" value="<bean:write name="shire" />" />
                      <input type="hidden" name="target" value="<bean:write name="target" />" />
                      <input type="hidden" name="providerId" value="<bean:write name="providerId" />" />
                      <logic:present name="time" scope="request">
                           <input type="hidden" name="time" value="<bean:write name="time" />" />
                      </logic:present>
                  </logic:notPresent>
                  <logic:present name="entityID" scope="request">
                      <input type="hidden" name="entityID" value="<bean:write name="entityID" />" />
                      <input type="hidden" name="returnX" value="<bean:write name="returnX" />" />
                      <input type="hidden" name="returnIDParam" value="<bean:write name="returnIDParam" />" />
                   </logic:present>
                   <input type="hidden" name="action" value="selection" />
                   <input type="submit" value="Select" tabindex="100" />
                   <select name="cache" tabindex="100" >
                     <option value="false"> Do not remember</option>
                     <option value="session" selected="selected"> Remember for session</option>
                     <option value="perm"> Remember for a week</option>
                   </select>
                      </p>
                   </div>
                </form>     
            </logic:present>
        </div>
    </div>

    <div class="footer">
        <p class="text">
<!--CONFIG-->
Need assistance? Send mail to <a tabindex="120" href="mailto:user@domain">administrator's name</a> with description.
        </p>
        <div class="logo"><img src="images/internet2.gif" alt="Internet2" /></div>
    </div>

<logic:present name="showComments" scope="Request">

<!--PROGRAMMING NOTE
  
  We need to program the on changed selector.  Note that option.InnterText only
  works on IE, options.remove doesn't work on Firefox, and that
  options.add doesn't work on Safari.  Hence the somewhat strange manipulations
  to delete & populate the list of options.

  X        is the select object for the right hand table
  Selected is the name selected in the left hand table

-->

</logic:present>

<logic:present name="siteLists" scope="request">
<script language="javascript" type="text/javascript">
<!--

function changedFed(X, Selected) {

  <logic:notPresent name="singleSiteList" scope="request">

     while (X.length > 0) {
        X.options[(X.length-1)] = null;
     }
  
  
    <logic:iterate id="siteset" name="siteLists">
      if (Selected == "<jsp:getProperty name="siteset" property="name"/>") {
        var opt;
        <logic:iterate id="site" name="siteset" property="sites">
          opt = new Option ("<jsp:getProperty name="site" property="displayName" />");
          X.options[X.length] = opt;
          opt.value = "<jsp:getProperty name="site" property="name" />";
        </logic:iterate>
      }
    </logic:iterate>
  
      if (Selected == "ALL") {
        var opt;
  
      <logic:present name="sites" scope="request">
          <logic:iterate id="site" name="sites">
            opt = new Option("<jsp:getProperty name="site" property="displayName" />");
            X.options[X.length] = opt;
            opt.value = "<jsp:getProperty name="site" property="name" />";
          </logic:iterate>
      </logic:present>
  
      <logic:notPresent name="sites" scope="request">
          <logic:iterate id="siteset" name="siteLists">
            <logic:iterate id="site" name="siteset" property="sites">
              opt = new Option ("<jsp:getProperty name="site" property="displayName" />");
              X.options[X.length] = opt;
              opt.value = "<jsp:getProperty name="site" property="name" />";
            </logic:iterate>
          </logic:iterate>
      </logic:notPresent>
    }
  
  </logic:notPresent>
  
}
-->
</script>
</logic:present>

<logic:present name="sites" scope="request">

<logic:present name="showComments" scope="Request">
   <!-- Load the autosuggest code.

        PROGRAMMING NOTE - the "ie6Hack" is to do with an issue in ie6 in which the
        psuedo drop down floats below the real dropdown.  The hack is that we jsut disable
        the real drop down when the pseudo one is about.  This can seem weird for some
        layouts and so if you are not deploying against ie6 you can just send an
        empty array.
 -->
</logic:present>
   <script language="javascript" type="text/javascript" src="Suggest.js"></script>
   <script language="javascript" type="text/javascript">
<!--  
window.onload = function() {

<logic:notPresent  name="siteLists" scope="request">
     var ie6Hack = [ document.getElementById("hackForie6")];
</logic:notPresent>

<logic:present name="siteLists" scope="request">
     var ie6Hack = [ document.getElementById("FedSelect"), document.getElementById("originIdp")];
</logic:present>
     var control = new TypeAheadControl(theElements,
             document.getElementById("enterText"),
             document.getElementById("enterOrigin"),
             document.getElementById("enterSubmit"),
             document.getElementById("enterType"),
             ie6Hack);


     document.getElementById("enterText").focus();
}


var theElements = [
  <logic:iterate id="site" name="sites">
     ["<%= ((edu.internet2.middleware.shibboleth.wayf.IdPSite)site).getDisplayName().replace("\n","").toString() %>",
      "<jsp:getProperty name="site" property="name" />"],
  </logic:iterate>
 ];

-->
   </script>
</logic:present>

  
</body>
</html>
  