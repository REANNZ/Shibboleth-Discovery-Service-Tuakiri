package edu.internet2.middleware.shibboleth.wayf.plugins.provider;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.xml.util.Base64;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.wayf.DiscoveryServiceHandler;
import edu.internet2.middleware.shibboleth.wayf.IdPSite;
import edu.internet2.middleware.shibboleth.wayf.WayfException;
import edu.internet2.middleware.shibboleth.wayf.plugins.Plugin;
import edu.internet2.middleware.shibboleth.wayf.plugins.PluginContext;
import edu.internet2.middleware.shibboleth.wayf.plugins.PluginMetadataParameter;
import edu.internet2.middleware.shibboleth.wayf.plugins.WayfRequestHandled;

/**
 * This is a test implementation of the saml cookie lookup stuff to 
 * see whether it fits the plugin architecture.
 * 
 * @author Rod Widdowson
 *
 */
public class SamlCookiePlugin implements Plugin {
        
    /**
     * The parameter which controls the cache.
     */
    private static final String PARAMETER_NAME = "cache";

    /**
     * Parameter to say make it last a long time.
     */
    private static final String PARAMETER_PERM = "perm";

    /**
     * Parameter to say just keep this as long as the brower is open.
     */
    private static final String PARAMETER_SESSION = "session";
    
    /**
     * Handle for logging. 
     */
    private static Logger log = Logger.getLogger(SamlCookiePlugin.class.getName());

    /**
     * As specified in the SAML2 profiles specification.
     */
    public static final String COOKIE_NAME = "_saml_idp";

    /**
      * The cookie to remember the permanent redirect
      */
    public static final String REDIRECT_COOKIE_NAME = "_saml_redirect";
        
    /**
     * The parameter name for setting permanent redirect
     */
    private static final String REDIRECT_PARAMETER_NAME = "redirect";

    /**
     * The parameter value for setting permanent redirect
     */
    private static final String REDIRECT_PARAMETER_VALUE = "redirect";

    /**
     * By default we keep the cookie around for a week.
     */
    private static final int DEFAULT_CACHE_EXPIRATION = 6048000;
    
    /**
     * Do we always go where the cookie tells us, or do we just provide the cookie as a hint.
     */
    private boolean alwaysFollow;

    /**
     * Is our job to clean up the cookie. 
     */
    private boolean deleteCookie;
    
    /**
     * Lipservice towards having a common domain cookie. 
     */
    private String cacheDomain; 
    
    /**
     * How long the cookie our will be active? 
     */
    private int cacheExpiration;
    
    /**
     * This constructor is called during wayf initialization with it's
     * own little bit of XML config.
     * 
     * @param element - further information to be gleaned from the DOM.
     */
    public SamlCookiePlugin(Element element) {
        /*
         * <Plugin idenfifier="WayfCookiePlugin" 
         *         type="edu.internet2.middleware.shibboleth.wayf.plugins.provider.SamlCookiePlugin"
         *         alwaysFollow = "FALSE"
         *         deleteCookie = "FALSE"
         *         cacheExpiration = "number" 
         *         cacheDomain = "string"/> 
         */
        log.info("New plugin");
        String s;

        s = element.getAttribute("alwaysFollow");
        if (s != null && !s.equals("") ) {
            alwaysFollow = Boolean.valueOf(s).booleanValue();
        } else {
            alwaysFollow = true;
        }
            
        s = element.getAttribute("deleteCookie");
        if (s != null && !s.equals("")) {
            deleteCookie = Boolean.valueOf(s).booleanValue();
        } else {
            deleteCookie = false;
        }
            
        s = element.getAttribute("cacheDomain");
        if ((s != null) && !s.equals("")) {
            cacheDomain = s;
        } else {
            cacheDomain = "";
        }
        
        s  = element.getAttribute("cacheExpiration");
        if ((s != null) && !s.equals("")) {
            
            try {

                cacheExpiration = Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                    
                log.error("Invalid CacheExpiration value - " + s);
                cacheExpiration = DEFAULT_CACHE_EXPIRATION;                       
            }
        } else {
            cacheExpiration = DEFAULT_CACHE_EXPIRATION;
        }
    }
    
    /**
     * Create a plugin with the hard-wired default settings.
     */
    private SamlCookiePlugin() {
        alwaysFollow = false;
        deleteCookie = false;
        cacheExpiration = DEFAULT_CACHE_EXPIRATION;
    }

    /**
     * This is the 'hook' in the lookup part of Discovery Service processing. 
     * 
     * @param req - Describes the current request.  Used to find any appropriate cookies 
     * @param res - Describes the current response.  Used to redirect the request. 
     * @param parameter - Describes the metadata.
     * @param context - Any processing context returned from a previous call. We set this on first call and
     *                  use non null to indicate that we don't go there again.
     * @param validIdps The list of IdPs which is currently views as possibly matches for the pattern. 
     *                  The Key is the EntityId for the IdP and the value the object which describes 
     *                  the Idp 
     * @param idpList The set of Idps which are currently considered as potential hints.    
     * @return a context to hand to subsequent calls
     * @throws WayfRequestHandled if the plugin has handled the request.
     * issues a redirect)
     * 
     * @see edu.internet2.middleware.shibboleth.wayf.plugins.Plugin#lookup
     */
    public PluginContext lookup(HttpServletRequest req,
                                HttpServletResponse res,  
                                PluginMetadataParameter parameter, 
                                Map<String, IdPSite> validIdps,
                                PluginContext context,
                                List <IdPSite> idpList) throws WayfRequestHandled {
            
        if (context != null) {
            //
            // We only need to be called once
            //
            return context;
        }
            
        if (deleteCookie) {
            deleteCookie(req, res);
            //
            // Only need to be called once - so set up a parameter
            //
            return new Context() ;
        } 
        List <String> idps = getIdPCookie(req, res, cacheDomain).getIdPList();

        boolean doRedirect = false;
        Cookie redirectCookie = getCookie(req, REDIRECT_COOKIE_NAME);
        if (redirectCookie != null && REDIRECT_PARAMETER_VALUE.equals(redirectCookie.getValue())) { doRedirect = true; };
        /* if the list of IdP cookies is not 1, do not redirect */
        if (idps.size() != 1) { doRedirect = false; };
            
        for (String idpName : idps) {
            IdPSite idp = validIdps.get(idpName);
            if (idp != null) {
                if (alwaysFollow || doRedirect) {
                    try {
                        DiscoveryServiceHandler.forwardRequest(req, res, idp);
                    } catch (WayfException e) {
                        // Do nothing we are going to throw anyway
                        ;
                    }
                    throw new WayfRequestHandled();
                }
                //
                // This IDP is ok 
                //
                // We should now add the IdP to the list.  But before doing so,
                // check it's not already there (from a different federation)
                // so that we are not adding it twice.
                // Compare based on IdPSite.getName - which returns the entityID

                boolean alreadyInList = false;
                for (IdPSite otherIdP:idpList)
                  if (otherIdP.getName().equals(idp.getName())) {
                    alreadyInList = true;
                  };
                if (alreadyInList) continue;
                    
                // now add the IdP to the list of hints
                idpList.add(idp);
            }
        } 
            
        return null;
    }

    /**
     * Plugin point which is called when the data is refreshed.
     * @param metadata - where to get the data from.
     * @return the value which will be provided as input to subsequent calls
     * @see edu.internet2.middleware.shibboleth.wayf.plugins.Plugin#refreshMetadata
     */
    public PluginMetadataParameter refreshMetadata(MetadataProvider metadata) {
        //
        // We don't care about metadata - we are given all that we need
        //
        return null;
    }

    /**
     * Plgin point for searching.
     * 
     * @throws WayfRequestHandled 
     * @param req Describes the current request. 
     * @param res Describes the current response.
     * @param parameter Describes the metadata.
     * @param pattern What we are searchign for. 
     * @param validIdps The list of IdPs which is currently views as possibly matches for the pattern. 
     *                  The Key is the EntityId for the IdP and the value the object which describes 
     *                  the Idp 
     * @param context Any processing context returned from a previous call. We set this on first call and
     *                use non null to indicate that we don't go there again.
     * @param searchResult What the search yielded. 
     * @param idpList The set of Idps which are currently considered as potential hints.    
     * @return a context to hand to subsequent calls.
     * @see edu.internet2.middleware.shibboleth.wayf.plugins.Plugin#search
     * @throws WayfRequestHandled if the plugin has handled the request.
     * 
     */
    public PluginContext search(HttpServletRequest req,
                                HttpServletResponse res, 
                                PluginMetadataParameter parameter, 
                                String pattern,
                                Map<String, IdPSite> validIdps,
                                PluginContext context,
                                Collection<IdPSite> searchResult,
                                List<IdPSite> idpList) throws WayfRequestHandled {
        //
        // Don't distinguish between lookup and search
        //
        return lookup(req, res, parameter, validIdps, context, idpList);
    }

    /**
     * Plugin point for selection.
     * 
     * @see edu.internet2.middleware.shibboleth.wayf.plugins.Plugin#selected(javax.servlet.http.HttpServletRequest.
     *  javax.servlet.http.HttpServletResponse, 
     *  edu.internet2.middleware.shibboleth.wayf.plugins.PluginMetadataParameter, 
     *  java.lang.String)
     * @param req Describes the current request. 
     * @param res Describes the current response.
     * @param parameter Describes the metadata.
     * @param idP Describes the idp.
     * 
     */
    public void selected(HttpServletRequest req, HttpServletResponse res,
                         PluginMetadataParameter parameter, String idP) {
            
        SamlIdPCookie cookie = getIdPCookie(req, res, cacheDomain);
        String param = req.getParameter(PARAMETER_NAME);
        String redirectParam = req.getParameter(REDIRECT_PARAMETER_NAME);
        int cookieExpiration = -1;
        
        if (null == param || param.equals("")) return;

        if (param.equalsIgnoreCase(PARAMETER_SESSION) || param.equalsIgnoreCase(PARAMETER_PERM) ) {
            cookie.addIdPName(idP);
        };

        if (param.equalsIgnoreCase(PARAMETER_PERM)) cookieExpiration = cacheExpiration;
        

        if (redirectParam != null && redirectParam.equals(REDIRECT_PARAMETER_VALUE) ) {
            log.debug("Setting permanent redirect cookie - valid for " + cookieExpiration + " seconds");

            // make sure the IdP list is just 1 entry long - ditch other entries if not
            if (cookie.getIdPList().size()>1) {
                log.info("IdP list is longer then 1, removing all except the most recent IdP to enable permanent redirect");
                cookie.getIdPList().clear();
                cookie.addIdPName(idP);
            }; 

            Cookie redirectCookie = new Cookie(REDIRECT_COOKIE_NAME, REDIRECT_PARAMETER_VALUE);
            redirectCookie.setComment("Used to determine whether to redirect the user to the IdP in future logins without asking again");
            redirectCookie.setPath(req.getContextPath()+"/"); // should be set to the servlet path
            redirectCookie.setMaxAge(cookieExpiration);
            // not setting domain on the redirectCookie - this one is for the DS only
            res.addCookie(redirectCookie);
        }

        cookie.writeCookie(cookieExpiration);

    }
    
    //
    // Private classes for internal use
    //
    
    /**
     * This is just a marker tag.
     */
    private static class Context implements PluginContext {}
    
    /** 
     * Class to abstract away the saml cookie for us.
     */
    public final class SamlIdPCookie  {

            
        /**
         * The associated request.
         */
        private final HttpServletRequest req;
        /**
         * The associated response.
         */
        private final HttpServletResponse res;
        /**
         * The associated domain.
         */
        private final String domain;
        /**
         * The IdPs.
         */
        private final List <String> idPList = new ArrayList<String>();
            
        /**
         * Constructs a <code>SamlIdPCookie</code> from the provided string (which is the raw data. 
         * 
         * @param codedData
         *            the information read from the cookie
         * @param request Describes the current request. 
         * @param response Describes the current response.
         * @param domainName - if non null the domain for any *created* cookie.
         */
        private SamlIdPCookie(String codedData, 
                              HttpServletRequest request, 
                              HttpServletResponse response, 
                              String domainName) {
                    
            this.req = request;
            this.res = response;
            this.domain = domainName;
                    
            int start;
            int end;
                    
            if (codedData == null || codedData.equals(""))  {
                log.info("Empty cookie");
                return;
            }
            //
            // An earlier version saved the cookie without URL encoding it, hence there may be 
            // spaces which in turn means we may be quoted.  Strip any quotes.
            //
            if (codedData.charAt(0) == '"' && codedData.charAt(codedData.length()-1) == '"') {
                codedData = codedData.substring(1,codedData.length()-1);
            }
                    
            try {
                codedData = URLDecoder.decode(codedData, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("could not decode cookie");
                return;
            }
                    
            start = 0;
            end = codedData.indexOf(' ', start);
            while (end > 0) {
                String value = codedData.substring(start, end);
                start = end + 1;
                end = codedData.indexOf(' ', start);
                if (!value.equals("")) {
                    idPList.add(new String(Base64.decode(value)));
                }
            }
            if (start < codedData.length()) {
                String value = codedData.substring(start);
                if (!value.equals("")) {
                    idPList.add(new String(Base64.decode(value)));
                }
            }
        }
        /**
         * Create a SamlCookie with no data inside.
         * @param domainName - if non null, the domain of the new cookie 
         * @param request Describes the current request. 
         * @param response Describes the current response.
         *
         */
        private SamlIdPCookie(HttpServletRequest request, HttpServletResponse response, String domainName) {
            this.req = request;
            this.res = response;
            this.domain = domainName;
        }

        /**
         * Add the specified Shibboleth IdP Name to the cookie list or move to 
         * the front and then write it back.
         * 
         * We always add to the front (and remove from wherever it was)
         * 
         * @param idPName    - The name to be added
         * @param expiration - The expiration of the cookie or zero if it is to be unchanged
         */
        private void addIdPName(String idPName) {

            idPList.remove(idPName);
            idPList.add(0, idPName);
        }
            
        /**
         * Delete the <b>entire<\b> cookie contents
         */


        /**
         * Remove origin from the cachedata and write it back.
         * 
         * @param origin what to remove.
         * @param expiration How long it will live.
         */
            
        public void deleteIdPName(String origin) {
            idPList.remove(origin);
        }

        /**
         * Write back the cookie.
         * 
         * @param expiration How long it will live
         */
        public void writeCookie(int expiration) {
            Cookie cookie = getCookie(req, COOKIE_NAME);
                    
            if (idPList.size() == 0) {
                //
                // Nothing to write, so delete the cookie
                //
                if (cookie != null) {
                    cookie.setPath(req.getContextPath()+"/");
                    cookie.setMaxAge(0);
                    log.debug("Deleting SAML Cookie " + COOKIE_NAME);
                    res.addCookie(cookie);
                } else {
                    log.debug("SAML Cookie " + COOKIE_NAME + " does not exist - nothing to delete");
                };
                return;
            }

            //
            // Otherwise encode up the cookie
            //
            StringBuffer buffer = new StringBuffer();
            Iterator <String> it = idPList.iterator();
                    
            while (it.hasNext()) {
                String next = it.next();
                String what = new String(Base64.encodeBytes(next.getBytes()));
                buffer.append(what).append(' ');
            }
                    
            String value;
            try {
                value = URLEncoder.encode(buffer.toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("Could not encode cookie");
                return;
            }
                    
            if (cookie == null) { 
                log.debug("SAML Cookie was null, creating new cookie");
                cookie = new Cookie(COOKIE_NAME, value);
            } else {
                cookie.setValue(value);
            }
            cookie.setComment("Used to cache selection of a user's Shibboleth IdP");
            cookie.setPath(req.getContextPath()+"/");


            cookie.setMaxAge(expiration);
                    
            if (domain != null && domain != "") {
                cookie.setDomain(domain);
            }
            log.debug("Adding SAML Cookie " + COOKIE_NAME);
            res.addCookie(cookie);
            
        }
    
        /**
         * Return the list of Idps for this cookie.
         * @return The list.
         */
        public List <String> getIdPList() {
            return idPList;
        }
    }

    /**
     * Extract the cookie from a request.
     * @param req the request.
     * @return the cookie.
     */
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

    /**
     * Delete the cookie from the response.
     * @param req The request.
     * @param res The response.
     */
    private static void deleteCookie(HttpServletRequest req, HttpServletResponse res) {
        Cookie cookie = getCookie(req, COOKIE_NAME);
            
        if (cookie != null) { 
            cookie.setPath("/");
            cookie.setMaxAge(0);
            res.addCookie(cookie);
        }

        /* also delete the redirect cookie */
        Cookie redirectCookie = getCookie(req, REDIRECT_COOKIE_NAME);
        if (redirectCookie != null) {
            redirectCookie.setPath("/");
            redirectCookie.setMaxAge(0);
            res.addCookie(redirectCookie);
        }
    }
    /**
     * Load up the cookie and convert it into a SamlIdPCookie.  If there is no
     * underlying cookie return a null one.
     * @param req The request.
     * @param res The response.
     * @param domain - if this is set then any <b>created</b> cookies are set to this domain
     * @return the new object. 
     */
    
    private SamlIdPCookie getIdPCookie(HttpServletRequest req, HttpServletResponse res, String domain) {
        Cookie cookie = getCookie(req, COOKIE_NAME);
            
        if (cookie == null) {
            return new SamlIdPCookie(req, res, domain);
        } else {
            return new SamlIdPCookie(cookie.getValue(), req, res, domain);
        }
    }
}

