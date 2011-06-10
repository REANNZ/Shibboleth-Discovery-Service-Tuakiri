/**
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.shibboleth.wayf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.samlext.idpdisco.DiscoveryResponse;
import org.opensaml.xml.XMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.shibboleth.common.ShibbolethConfigurationException;
import edu.internet2.middleware.shibboleth.wayf.plugins.Plugin;
import edu.internet2.middleware.shibboleth.wayf.plugins.PluginContext;
import edu.internet2.middleware.shibboleth.wayf.plugins.PluginMetadataParameter;
import edu.internet2.middleware.shibboleth.wayf.plugins.WayfRequestHandled;

/**
  * Specific handler for each version of the Discovery Service. 
  */
public class DiscoveryServiceHandler {

    /*
     * Protcol parameters - Old.
     */
    /**
     * Shire is the SP Assertion Consumer endpoint.
     */
    private static final String SHIRE_PARAM_NAME = "shire";
    /**
     * TargetName is where we are trying to get to.
     */
    private static final String TARGET_PARAM_NAME = "target";
    /**
     * time is to do with replay attack.
     */
    private static final String TIME_PARAM_NAME = "time";
    /**
     * This is the ID (in the metadata) of the SP.
     */
    private static final String PROVIDERID_PARAM_NAME = "providerId";
    
    /**
     * The entityDescriptor for the SP (if present).
     */
    private static final String PROVIDERID_OBJECT_PARAM_NAME = "providerObject"; 
    /*
     * Protocol parameters - New
     */
    /**
     * The SP id.
     */
    private static final String ENTITYID_PARAM_NAME = "entityID";
    /**
     * Where to send the request back to.
     */
    private static final String RETURN_PARAM_NAME = "return";
    /**
     * "return" is an invalid attribute, so we use returnX.
     */
    private static final String RETURN_ATTRIBUTE_NAME = "returnX";
    /**
     * Alternatively the index of where to send the address back to.
     */
    private static final String RETURN_INDEX_NAME = "returnIndex";
    
    /**
     * What value to put the ID of the selected metadata into.
     */
    private static final String RETURNID_PARAM_NAME = "returnIDParam";
    
    /**
     * What returnIDParam defaults to.
     */
    private static final String RETURNID_DEFAULT_VALUE = "entityID";
    /**
     * Whether we are allowed to interact.
     */
    private static final String ISPASSIVE_PARAM_NAME = "isPassive";
    
    /**
     * Whether we understand this or not.
     */
    private static final String POLICY_PARAM_NAME = "policy";

    /**
     * The only policy we know about.
     */
    private static final String KNOWN_POLICY_NAME 
        = "urn:oasis:names:tc:SAML:profiles:SSO:idp-discoveryprotocol:single";
    
    /**
     * Mandatory Serialization constant.
     */
    private static final  Logger LOG = LoggerFactory.getLogger(DiscoveryServiceHandler.class.getName());

    /**
     * The location defines the last part of the URL which distinguished this handler. 
     */
    private final String location;
    
    /**
     * If isDefault is true then if there is a mismatch then this handler is used.  
     */
    private final boolean isDefault;
    
    /**
     * Config handles detailed behavior.
     */
    private final HandlerConfig config;
    
    /**
     * The list of all the metadata providers that this discovery handler believes in.
     */
    private final List <IdPSiteSet> siteSets;
    
    /**
     * The list of all the plugins that this hanlder has had configured.
     */
    private final List <Plugin> plugins;
    
    /**
     * Constructor to create and configure the handler.
     * @param config - DOM Element with configuration information.
     * @param federations - Supplies all known providers which will be included if so configured.
     * @param plugins - Supplies all known plugins which will be included if configured in. 
     * @param defaultConfig - The default configurations.
     * @throws ShibbolethConfigurationException - if we find something odd in the config file. 
     */
    protected DiscoveryServiceHandler(Element config, 
                                      Hashtable <String, IdPSiteSet> federations,
                                      Hashtable <String, Plugin> plugins, 
                                      HandlerConfig defaultConfig) throws ShibbolethConfigurationException
    {
        siteSets = new ArrayList <IdPSiteSet>(federations.size());
        this.plugins = new ArrayList <Plugin>(plugins.size());

        //
        // Collect the Configuration from the XML
        //
        
        this.config = new HandlerConfig(config, defaultConfig);
        
        location = config.getAttribute("location");
        
        if (location == null || location.equals("")) {
                
                LOG.error("DiscoveryService must have a location specified");
                throw new ShibbolethConfigurationException("DiscoveryService must have a location specified");  
        }
        
        //
        // Is this the default WAYF?
        //
        
        String attribute = config.getAttribute("default");
        if (attribute != null && !attribute.equals("")) {
                isDefault = Boolean.valueOf(attribute).booleanValue();
        } else {
                isDefault = false;
        }
        
        //
        // Which federations (sitesets) do we care about?
        //
        
        NodeList list = config.getElementsByTagName("Federation");
                
        for (int i = 0; i < list.getLength(); i++ ) {
                    
            attribute = ((Element) list.item(i)).getAttribute("identifier");
                    
                IdPSiteSet siteset = federations.get(attribute);
                
                if (siteset == null) {
                    LOG.error("Handler " + location + ": could not find metadata for <Federation> with identifier " + attribute + ".");
                    throw new ShibbolethConfigurationException(
                           "Handler " + location + ": could not find metadata for  <Federation> identifier " + attribute + ".");
                }
                
                siteSets.add(siteset);
        }

        if (siteSets.size() == 0) {
            //
            // No Federations explicitly named pick em all
            //
            siteSets.addAll(federations.values());
        }
        
        //
        // Now, which plugins?
        //

        list = config.getElementsByTagName("PluginInstance");
        
        for (int i = 0; i < list.getLength(); i++ ) {
                    
            attribute = ((Element) list.item(i)).getAttribute("identifier");
                    
                Plugin plugin = plugins.get(attribute);
                
                if (plugin == null) {
                    LOG.error("Handler " + location + ": could not find plugin for identifier " + attribute);
                    throw new ShibbolethConfigurationException(
                              "Handler " + location + ": could not find plugin for identifier " + attribute);
                }
                
                this.plugins.add(plugin);
        }

        //
        // So now tell every IdPSite about every plugin.
        //
        // Note that there is only one idpsite per metadatafile per WAYF and that the discovery 
        // services share them, so the data explosion is only number(IdpSites) * number(Plugins) not
        // number(IdpSites) * number(Plugins) * number(DiscoverHandlers)
        
        for (IdPSiteSet site: siteSets) {
            for (Plugin plugin: this.plugins) {
                site.addPlugin(plugin);
            }
        }
    }
    
    
    //
    // Standard Beany Methods
    //
    /**
     * The 'Name' of the service. the path used to identify the ServiceHandler.
     * @return the path used to identify the ServiceHandler.
     */
    
    protected String getLocation() {
        return location;
    }

    /**
     * Whether this is the default service.
     * @return is it?
     */
    protected boolean isDefault() {
        return isDefault;
    }
    
    //
    // Now the bits that deal with the user request
    //

    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        
        String policy = req.getParameter(POLICY_PARAM_NAME);
        
        if (null != policy && !KNOWN_POLICY_NAME.equals(policy)) {
            //
            // unknown policy
            //
            LOG.error("Unknown policy " + policy);
            handleError(req, res, "Unknown policy " + policy);
            return;
        }
        
        //
        // Decide how to route the request based on query string
        //
        String requestType = req.getParameter("action");
        
        if (requestType == null || requestType.equals("")) {
            requestType = "lookup";
        }
        
        try {

            if (requestType.equals("search")) {
                    
                String parameter = req.getParameter("string"); 
                if (parameter != null && parameter.equals("")) {
                        parameter = null;
                }
                handleLookup(req, res, parameter);
                    
            } else if (requestType.equals("selection")) {
                    
                handleSelection(req, res);
            } else {
                handleLookup(req, res, null);
            }
        } catch (WayfException we) {
            LOG.error("Error processing DS request:", we);
            handleError(req, res, we.getLocalizedMessage());
        } catch (WayfRequestHandled we) {
            //
            // Yuck - a sucess path involving an exception
            //
        }

    }
        
    /**
     * When the WAYF user has selected something we look it up, tell the plugins and then dispatch to the Idp.
     *  
     * @param req - standard J2EE stuff
     * @param res - standard J2EE stuff
     * @throws WayfRequestHandled - if one of the plugins has done the dispatch
     * @throws WayfException - if we had an errors
     */
    private void handleSelection(HttpServletRequest req, 
                                 HttpServletResponse res) throws WayfRequestHandled, WayfException 
     {
            
        String idpName = req.getParameter("origin");
        LOG.debug("Processing handle selection: " + idpName);

        String sPName = getSPId(req);

        if (idpName == null || idpName.equals("")) {
            handleLookup(req, res, null);
            return;
        }

        if (getValue(req, SHIRE_PARAM_NAME) == null) {
            //
            // 2.0 protocol
            //
            setupReturnAddress(sPName, req);
        }
        //
        // Notify plugins
        //
        IdPSite site = null; 
        
        for (Plugin plugin:plugins) {
            for (IdPSiteSet idPSiteSet: siteSets) {
                PluginMetadataParameter param = idPSiteSet.paramFor(plugin);
                plugin.selected(req, res, param, idpName);
                if (site == null && idPSiteSet.containsIdP(idpName)) {
                    site = idPSiteSet.getSite(idpName);
                }
            }
        }
        
        if (site == null) {
            handleLookup(req, res, null);
        } else {
            forwardRequest(req, res, site);
        }
    }


    /**
     * This sets up the parameter RETURN_ATTRIBUTE_NAME with the return address 
     * harvested from the reqest.
     * <ul><le>If a "return" parameter is present we check in the metadata for spoofing 
     *         and then set up from there </le>
     *     <le>If "returnID" is specified we get this from the metadata</le>
     *     <le>If nothing is provided we get the default from the metadata (if provided)</le>
     *     <le>Otherwise we whine</le>
     * </ul>     
     * @param spName - the name of the Service provider.
     * @param req - The request.
     * @throws WayfException - if we spot spoofing or there is no defaumlt 
     */
    private void setupReturnAddress(String spName, HttpServletRequest req) throws WayfException{
        
        DiscoveryResponse[] discoveryServices;
        Set<XMLObject> objects = new HashSet<XMLObject>();
        String defaultName = null;
        boolean foundSPName = false;
        
        for (IdPSiteSet metadataProvider:siteSets) {
            
            //
            // Only do work if the SP makes sense
            //

            if (metadataProvider.containsSP(spName)) {
               
                //
                // The name makes sense so let's get the entity and from that
                // all of its roles
                //
                foundSPName = true;
                EntityDescriptor entity = metadataProvider.getEntity(spName);
                List<RoleDescriptor> roles = entity.getRoleDescriptors();
                
                for (RoleDescriptor role:roles) {
                    
                    //
                    // Check every role
                    //
                    
                    if (role instanceof SPSSODescriptor) {
                        
                        //
                        // And grab hold of all the extensions for SPSSO descriptors
                        //
                        
                        Extensions exts = role.getExtensions();
                        if (exts != null) {
                            objects.addAll(exts.getOrderedChildren());
                        }
                    }
                }
            }
        }
        if (!foundSPName) {
            LOG.error("Could not locate SP " + spName + " in metadata");
        }
        
        //
        // Now, let's strip out everything which isn't a DiscoveryService
        //
        
        discoveryServices = new DiscoveryResponse[objects.size()];
        int dsCount = 0;
        
        for (XMLObject obj:objects) {
            if (obj instanceof DiscoveryResponse) {
                DiscoveryResponse ds = (DiscoveryResponse) obj;
                discoveryServices[dsCount++] = ds;
                if (ds.isDefault() || null == defaultName) {
                    defaultName = ds.getLocation();
                }
            }
        }
        
        //
        // Now process the return parameters.  The name is either a parameter
        // called RETURN_PARAM_NAME or an attributes called RETURN_ATTRIBUTE_NAME
        //
        String returnName = req.getParameter(RETURN_PARAM_NAME);
        
        if (returnName == null || returnName.length() == 0) {
            returnName = getValue(req, RETURN_ATTRIBUTE_NAME);
        }
        
        //
        // Return index is only ever a parameter
        //
        
        String returnIndex = req.getParameter(RETURN_INDEX_NAME);
        
        if (returnName != null && returnName.length() != 0) {
            //
            // Given something so we have to police it.
            //
            String nameNoParam = returnName;
            URL providedReturnURL;
            int index = nameNoParam.indexOf('?');
            boolean found = false;
            
            if (index >= 0) {
                nameNoParam = nameNoParam.substring(0,index);
            }
            
            try {
                providedReturnURL = new URL(nameNoParam);                
            } catch (MalformedURLException e) {
                throw new WayfException("Couldn't parse provided return name " + nameNoParam, e);
            }
            
            
            for (DiscoveryResponse disc: discoveryServices) {
                if (equalsURL(disc, providedReturnURL)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new WayfException("Couldn't find endpoint " + nameNoParam + " in metadata");
            }
        } else if (returnIndex != null && returnIndex.length() != 0) {
            
            int index; 
            try {
                index = Integer.parseInt(returnIndex);
            } catch (NumberFormatException e) {
                throw new WayfException("Couldn't convert " + returnIndex + " into an index");
            }
            //
            // So look through to find the endpoint with the correct index
            //
            boolean found = false;
            
            for (DiscoveryResponse disc: discoveryServices) {
                if (index  == disc.getIndex()) {
                    found = true;
                    returnName = disc.getLocation();
                    break;
                }
            }
            if (!found) {
                throw new WayfException("Couldn't not find endpoint " + returnIndex + "in metadata");
            }
        } else {
            //
            // No name, not index, so we want the default
            //
            returnName = defaultName;
        }
        //
        // So by now returnName has the correct value, either harvested from or
        // policed against the metadata
        //
        req.setAttribute(RETURN_ATTRIBUTE_NAME, returnName);
    }

    /**
     * Helper function to see whather the provided endpoint in the metadata matches the 
     * provided return URL in the request.
     * 
     * @param discovery
     * @param providedName
     * @return
     */
    private static boolean equalsURL(DiscoveryResponse discovery, URL providedName) {
        
        //
        // Nothing provided - no match
        //
        if (null == discovery) {
            return false;
        }
        
        URL discoveryName;
        try {
            discoveryName = new URL(discovery.getLocation());
        } catch (MalformedURLException e) {
            //
            // Something bad happened.  Log it (it is only of interest to the sysadmin, not to the user)
            //
            LOG.warn("Found invalid discovery end point : " + discovery.getLocation(), e);
            return false;
        }
        
        return providedName.equals(discoveryName);
        
    }

    /**
     * Displays a Discovery Service selection page, having first consulted the plugins as needed.
     * @param req Describes the request
     * @param res Describes the response
     * @param searchName What are we looking for?
     * 
     * @throws WayfRequestHandled if a plugin has dealt with the request
     * @throws WayfException in case of an error.
     */
    private void handleLookup(HttpServletRequest req, 
                              HttpServletResponse res, 
                              String searchName) throws WayfException, WayfRequestHandled {
        
        String shire = getValue(req, SHIRE_PARAM_NAME);
        String providerId = getSPId(req);
        EntityDescriptor sp = null;
        boolean twoZeroProtocol = (shire == null);
        boolean isPassive = (twoZeroProtocol && 
                             "true".equalsIgnoreCase(getValue(req, ISPASSIVE_PARAM_NAME)));

        Collection <IdPSiteSetEntry> siteLists = null;
        Collection<IdPSite> searchResults = null;
        
        if (config.getProvideListOfLists()) {
            siteLists = new ArrayList <IdPSiteSetEntry>(siteSets.size());
        }

        Collection <IdPSite> sites = null;
        Comparator<IdPSite> comparator = new IdPSite.Compare(req);
       
        if (config.getProvideList()) {
            sites = new TreeSet<IdPSite>(comparator);
        }

        if (searchName != null && !searchName.equals("")) {
            searchResults = new TreeSet<IdPSite>(comparator);
        }

        LOG.debug("Processing Idp Lookup for : " + providerId);

        //
        // Iterate over all the sitesets and if they know about the SP pass them to the plugins
        // and then add them too the list
        //

        PluginContext[] ctx = new PluginContext[plugins.size()];
        List<IdPSite> hintList = new ArrayList<IdPSite>();
        
        if (twoZeroProtocol) {
            setupReturnAddress(providerId, req);
        }
        //
        // By having siteLists and sites as parameters we only iterate over 
        // the metadata arrays once.
        //
        try {   
            for (IdPSiteSet metadataProvider:siteSets) {
                
                //
                // Only do work if the SP makes sense
                //

                if (metadataProvider.containsSP(providerId) || !config.getLookupSp()) {

                    Collection <IdPSite> search = null;

                    if (null == sp) {
                        sp = metadataProvider.getEntity(providerId);
                    }
                    
                    if (searchResults != null) {
                        search = new TreeSet<IdPSite>(comparator);
                    }

                    Map <String, IdPSite> theseSites = metadataProvider.getIdPSites(searchName, config, search);
                    
                    //
                    // Consult the plugins
                    //
                    for (int i = 0; i < plugins.size(); i++) {
                            
                        Plugin plugin = plugins.get(i);
                        
                        if (searchResults == null) {
                            //
                            // This was a search
                            //
                            ctx[i] = plugin.lookup(req, 
                                                   res, 
                                                   metadataProvider.paramFor(plugin), 
                                                   theseSites, 
                                                   ctx[i], 
                                                   hintList);
                        } else {
                            ctx[i] = plugin.search(req, 
                                                   res, 
                                                   metadataProvider.paramFor(plugin), 
                                                   searchName, 
                                                   theseSites, 
                                                   ctx[i], 
                                                   searchResults, 
                                                   hintList);
                        }
                    }
                    
                    if (null == theseSites || theseSites.isEmpty()) {
                        continue;
                    }
                    
                    //
                        
                    // Accumulate any per-metadata provider information
                    // 
            
                    Collection<IdPSite> values = new TreeSet<IdPSite>(comparator);
                    if (null != theseSites) {
                        values.addAll(theseSites.values());
                    }
                    
                    if (siteLists != null) {
                        siteLists.add(new IdPSiteSetEntry(metadataProvider,values));
                    }
                            
                    if (sites != null) {
                        sites.addAll(values);
                    }
                    
                    if (searchResults != null) {
                        searchResults.addAll(search);
                    }
                }
            }
            
            if (isPassive) {
                //
                // No GUI intervention.
                //
                if (0 != hintList.size()) {
                    //
                    // We found a candidate, hand it back
                    //
                    forwardRequest(req, res, hintList.get(0));
                } else {
                    forwardRequest(req, res, null);
                }   
                return;
            }
            
            //
            // Now set up all the funky stuff that the JSP needs.  Firstly the protocol
            // specific parameters which will come back to us
            //
            
            if (twoZeroProtocol) {
                //
                // The return address was set up in setupReturnAddress
                //
                String returnString = (String) req.getAttribute(RETURN_ATTRIBUTE_NAME);
                if (null == returnString || 0 == returnString.length()) {
                    throw new WayfException("Parameter " + RETURN_PARAM_NAME + " not supplied");
                }

                String returnId = getValue(req, RETURNID_PARAM_NAME);
                if (null == returnId || 0 == returnId.length()) {
                    returnId = RETURNID_DEFAULT_VALUE;
                }
                //
                // Return *means* something so we cannot use it as an attribute
                //
                req.setAttribute(RETURN_ATTRIBUTE_NAME, returnString);
                req.setAttribute(RETURNID_PARAM_NAME, returnId);
                req.setAttribute(ENTITYID_PARAM_NAME, providerId);
                
            } else {
                String target = getValue(req, TARGET_PARAM_NAME);
                if (null == target || 0 == target.length()) {
                    throw new WayfException("Could not extract target from provided parameters");
                }
                req.setAttribute(SHIRE_PARAM_NAME, shire);
                req.setAttribute(TARGET_PARAM_NAME, target);
                req.setAttribute(PROVIDERID_PARAM_NAME, providerId);
                //
                // Time is in unix format
                //
                req.setAttribute("time", new Long(new Date().getTime() / 1000).toString()); 
                
            }
            
            //
            // Setup the stuff that the GUI wants.  
            //
            setDisplayLanguage(sites, req);
            req.setAttribute("sites", sites);
            if (null != siteLists) {
                for (IdPSiteSetEntry siteSetEntry:siteLists) {
                    setDisplayLanguage(siteSetEntry.getSites(), req);
                }
            }
            
            req.setAttribute(PROVIDERID_OBJECT_PARAM_NAME, sp);
                
            req.setAttribute("siteLists", siteLists);
            req.setAttribute("requestURL", req.getRequestURI().toString());

            if (searchResults != null) {
                if (searchResults.size() != 0) {
                    setDisplayLanguage(searchResults, req);
                    req.setAttribute("searchresults", searchResults);
                } else {
                    req.setAttribute("searchResultsEmpty", "true");
                }
            }

            if (hintList.size() > 0) {
                setDisplayLanguage(hintList, req);
                req.setAttribute("cookieList", hintList);
            }

            LOG.debug("Displaying WAYF selection page.");
            RequestDispatcher rd = req.getRequestDispatcher(config.getJspFile());

            //
            // And off to the jsp
            //
            rd.forward(req, res);
        } catch (IOException ioe) {
            LOG.error("Problem displaying WAYF UI.\n" +  ioe.getMessage());
            throw new WayfException("Problem displaying WAYF UI", ioe);
        } catch (ServletException se) {
            LOG.error("Problem displaying WAYF UI.\n" +  se.getMessage());
            throw new WayfException("Problem displaying WAYF UI", se);
        }
    }

    /**
     * Prior to display we set the display language from the
     * browser. There is probably a proper way to do this using
     * jsp, but I want to keep the API between JSP and java the same 1.3->2.0
     * @param sites - the sites we need to impact
     * @param req - from whiuch we get the locale
     */
    private void setDisplayLanguage(Collection<IdPSite> sites, HttpServletRequest req) {
        
        if (null == sites) {
            return;
        }
        Locale locale = req.getLocale();
        if (null == locale) {
            Locale.getDefault();
        }
        String lang = locale.getLanguage();

        for (IdPSite site : sites) {
            site.setDisplayLanguage(lang);
        }
    }


    /**
     * Uses an HTTP Status 307 redirect to forward the user to the IdP or the SP.
     * 
     * @param req request under consideration
     * @param res response under construction
     * @param site The Idp
     * @throws WayfException if bad things happen.
     */
    public static void forwardRequest(HttpServletRequest req, HttpServletResponse res, IdPSite site)
                    throws WayfException {

        String shire = getValue(req, SHIRE_PARAM_NAME);
        String providerId = getSPId(req);
        boolean twoZeroProtocol = (shire == null);

        if (!twoZeroProtocol) {
            String handleService = site.getAddressForWAYF(); 
            if (handleService != null ) {

                String target = getValue(req, TARGET_PARAM_NAME);
                if (null == target || 0 == target.length()) {
                    throw new WayfException("Could not extract target from provided parameters");
                }

                LOG.info("Redirecting to selected Handle Service: " + handleService);
                try {
                    StringBuffer buffer = new StringBuffer(handleService +  
                       "?" + TARGET_PARAM_NAME + "=");
                    buffer.append(URLEncoder.encode(target, "UTF-8"));
                    buffer.append("&" + SHIRE_PARAM_NAME + "=");
                    buffer.append(URLEncoder.encode(shire, "UTF-8"));
                    buffer.append("&" + PROVIDERID_PARAM_NAME + "=");
                    buffer.append(URLEncoder.encode(providerId, "UTF-8"));
                         
                    //
                    // Time is as per U**X
                    //
                    buffer.append("&" +  TIME_PARAM_NAME + "=");
                    buffer.append(new Long(new Date().getTime() / 1000).toString());
                    res.sendRedirect(buffer.toString());
                } catch (IOException ioe) {
                    //
                    // That failed.  
                    //
                    throw new WayfException("Error forwarding to IdP: \n" + ioe.getMessage());
                }
            } else {
                String s = "Error finding to IdP: " + site.getDisplayName(req); 
                LOG.error(s);
                throw new WayfException(s);
            }
        } else {
            String returnUrl = (String) req.getAttribute(RETURN_ATTRIBUTE_NAME);
            
            if (null == returnUrl || 0 == returnUrl.length()) {
                throw new WayfException("Could not find return parameter");
            }
            try {
                returnUrl = URLDecoder.decode(returnUrl, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new WayfException("Did not understand parameter ", e);
            }
            String redirect;
            if (site != null) {
                StringBuffer buffer = new StringBuffer(returnUrl);
                //
                // If we were given anybody to lookup, construct the URL
                //
                String returnParam = getValue(req, RETURNID_PARAM_NAME);
                if (null == returnParam || 0 == returnParam.length()) {
                    returnParam = RETURNID_DEFAULT_VALUE;
                }              
                //
                // Do we add a '?' or a '&' for the parameters
                //

                if (returnUrl.indexOf('?') >= 0) {
                    //
                    // there is a parameter already.  Add a '&'
                    //
                    buffer.append("&" + returnParam + "=");
                } else {
                    //
                    // No parameter.  Use ?
                    //
                    buffer.append("?" + returnParam + "=");
                }
                buffer.append(site.getName());
                redirect =  buffer.toString();
            } else {
                //
                // Just send it back
                //
                redirect = returnUrl;
            }
            
            LOG.debug("Dispatching to " + redirect);
            
            try {
                res.sendRedirect(redirect);
            } catch (IOException ioe) {
                //
                // That failed.  
                //
                throw new WayfException("Error forwarding back to Sp: \n" + ioe.getMessage());
            }         
        }
    }

    /**
     * Handles all "recoverable" errors in WAYF processing by logging the error and forwarding the user to an
     * appropriate error page.
     * 
     * @param req request under consideration
     * @param res response under construction
     * @param message - what so say
     */
    private void handleError(HttpServletRequest req, HttpServletResponse res, String message) {

        LOG.debug("Displaying WAYF error page.");
        req.setAttribute("errorText", message);
        req.setAttribute("requestURL", req.getRequestURI().toString());
        RequestDispatcher rd = req.getRequestDispatcher(config.getErrorJspFile());

        try {
            rd.forward(req, res);
        } catch (IOException ioe) {
            LOG.error("Problem trying to display WAYF error page: " + ioe.toString());
        } catch (ServletException se) {
            LOG.error("Problem trying to display WAYF error page: " + se.toString());
        }
    }

    /**
     * Gets the value for the parameter either from the parameter or from jsp.
     * @param req - the request.
     * @param name - the name of the parameter.
     * @return - result
     */
    private static String getValue(HttpServletRequest req, String name) {

        
        String value = req.getParameter(name); 
        if (value != null) {
            return value;
        }
        return (String) req.getAttribute(name);
    }

    private static String getSPId(HttpServletRequest req) throws WayfException {

        //
        // Try first with 2.0 version
        //
        String param = req.getParameter(ENTITYID_PARAM_NAME);
        if (param != null && !(param.length() == 0)) {
            return param;
        } 
        
        param = (String) req.getAttribute(ENTITYID_PARAM_NAME);
        if (param != null && !(param.length() == 0)) {
            return param;
        }       
        //
        // So Try with 1.3 version
        //
        param = req.getParameter(PROVIDERID_PARAM_NAME);
        if (param != null && !(param.length() == 0)) {
            return param;
        } 
        
        param = (String) req.getAttribute(PROVIDERID_PARAM_NAME);
        if (param != null && !(param.length() == 0)) {
            return param;
        } 
        throw new WayfException("Could not locate SP identifier in parameters");
    }   
}
