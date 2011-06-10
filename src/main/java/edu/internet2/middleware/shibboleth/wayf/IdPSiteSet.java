/*
 * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.]
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

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.Organization;
import org.opensaml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml2.metadata.OrganizationName;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.FileBackedHTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataFilter;
import org.opensaml.saml2.metadata.provider.MetadataFilterChain;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ObservableMetadataProvider;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.parse.ParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.shibboleth.common.ShibbolethConfigurationException;
import edu.internet2.middleware.shibboleth.wayf.plugins.Plugin;
import edu.internet2.middleware.shibboleth.wayf.plugins.PluginMetadataParameter;
import edu.internet2.middleware.shibboleth.wayf.plugins.provider.BindingFilter;

/**
 * 
 * Represents a collection of related sites as described by a single source of metadata. 
 * This is usually a federation.  When the WAYF looks to see which IdP sites to show, 
 * it trims the list so as to not show IdP's which do not trust the SP.
 *
 * This class is opaque outside this file.  The three static methods getSitesLists,
 * searchForMatchingOrigins and lookupIdP provide mechansims for accessing 
 * collections of IdPSiteSets.
 * 
 */

public class IdPSiteSet implements ObservableMetadataProvider.Observer {
        
    /** Handle for error output. */
    private static final Logger LOG = LoggerFactory.getLogger(IdPSiteSet.class.getName());

    /** The OpenSaml metadata source. */
    private ObservableMetadataProvider metadata;

    /** Is the named SP in the current metadata set? */
    private Set<String> spNames = new HashSet<String>(0);

    /** Is the named IdP in the current metadata set? */
    private Set<String> idpNames = new HashSet<String>(0);
    
    /** What does the configuration identify this as? */
    private final String identifier;
    
    /** What name should we display for this set of entities? */
    private final String displayName;
    
    /** Where does the metadata exist? */
    private String location;
    
    /** What parameters do we pass in to which plugin? */
    private final Map<Plugin, PluginMetadataParameter> plugins = new HashMap<Plugin, PluginMetadataParameter>();
    
    /**
     * Create a new IdPSiteSet as described by the supplied XML segment. 
     * @param el - configuration details.
     * @param parserPool - the parsers we initialized above.
     * @param warnOnBadBinding if we just warn or give an error if an SP has bad entry points.
     * @throws ShibbolethConfigurationException - if something goes wrong.
     */
    protected IdPSiteSet(Element el, ParserPool parserPool, boolean warnOnBadBinding) throws ShibbolethConfigurationException {

        String spoolSpace;
        String delayString;

        this.identifier = el.getAttribute("identifier");
        this.displayName = el.getAttribute("displayName");
        location = el.getAttribute("url");
        if (null == location || location.length() == 0) {
            //
            // Sigh for a few releases this was documented as URI
            //
            location = el.getAttribute("url");
        }
        spoolSpace = el.getAttribute("backingFile");
        delayString = el.getAttribute("timeout");
        
        //
        // Configure the filters (before the metadata so we can add them before we start reading)
        //
        String ident;
        String className;
        ident = "<not specified>"; 
        className = "<not specified>"; 
        MetadataFilterChain filterChain = null;
        filterChain = new MetadataFilterChain();
        try {
            NodeList itemElements = el.getElementsByTagNameNS(XMLConstants.CONFIG_NS, "Filter");
            List <MetadataFilter> filters = new ArrayList<MetadataFilter>(1 + itemElements.getLength());
            
            //
            // We always have a binding filter
            //
            filters.add(new BindingFilter(warnOnBadBinding));
                
            for (int i = 0; i < itemElements.getLength(); i++) {
                Element element = (Element) itemElements.item(i);
   
                ident = "<not specified>"; 
                className = "<not specified>"; 
            
                ident = element.getAttribute("identifier");

                if (null == ident || ident.equals("")) {
                    LOG.error("Could not load filter with no identifier");
                    continue;
                }
            
                className = element.getAttribute("type");
                if (null == className || className.equals("")) {
                    LOG.error("Filter " + identifier + " did not have a valid type");
                }
                //
                // So try to get hold of the Filter
                //
                Class<MetadataFilter> filterClass = (Class<MetadataFilter>) Class.forName(className);
                Class[] classParams = {Element.class};
                Constructor<MetadataFilter> constructor = filterClass.getConstructor(classParams);
                Object[] constructorParams = {element};
            
                filters.add(constructor.newInstance(constructorParams));
            }
            filterChain.setFilters(filters);
        } catch (Exception e) {
            LOG.error("Could not load filter " + ident + "()" + className + ") for " + this.identifier, e);
            throw new ShibbolethConfigurationException("Could not load filter", e);
        }
    
        LOG.info("Loading Metadata for " + displayName);
        try {
            int delay;
            delay = 30000;
            if (null != delayString && !"".equals(delayString)) {
                delay = Integer.parseInt(delayString);
            }
            
            URL url = new URL(location); 
            if ("file".equalsIgnoreCase(url.getProtocol())){
                FilesystemMetadataProvider provider = new FilesystemMetadataProvider(new File(url.getFile()));
                provider.setParserPool(parserPool);
                if (null != filterChain) {
                    provider.setMetadataFilter(filterChain);
                }
                provider.initialize();
                metadata = provider;
            } else {
                if (spoolSpace == null || "".equals(spoolSpace)) {
                    throw new ShibbolethConfigurationException("backingFile must be specified for " + identifier);
                }
                
                FileBackedHTTPMetadataProvider provider;
            
                provider = new FileBackedHTTPMetadataProvider(location, delay, spoolSpace);
                provider.setParserPool(parserPool);
                if (null != filterChain) {
                    provider.setMetadataFilter(filterChain);
                }
                provider.initialize();
                metadata = provider;
            }
        } catch (MetadataProviderException e) {
            throw new ShibbolethConfigurationException("Could not read " + location, e);
        } catch (NumberFormatException e) {
            throw new ShibbolethConfigurationException("Badly formed timeout " + delayString, e);
        } catch (MalformedURLException e) {
            throw new ShibbolethConfigurationException("Badly formed url ", e);
        }
        metadata.getObservers().add(this);
        onEvent(metadata);
    }

    /**
     * Based on 1.2 Origin.isMatch.  There must have been a reason for it...
     * [Kindas of] support for the search function in the wayf.  This return many false positives
     * but given the aim is to provide input for a pull down list...
     * 
     * @param entity   The entity to match.
     * @param str      The patten to match against.
     * @param config   Provides list of tokens to not lookup
     * @return         Whether this entity matches  
     */

    private static boolean isMatch(EntityDescriptor entity, String str, HandlerConfig config) {
        
        Enumeration input = new StringTokenizer(str);
        while (input.hasMoreElements()) {
            String currentToken = (String) input.nextElement();

            if (config.isIgnoredForMatch(currentToken)) {                           
                continue;
            }
                
            currentToken = currentToken.toLowerCase(); 

            if (entity.getEntityID().indexOf(currentToken) > -1) {
                return true; 
            }
                                
            Organization org = entity.getOrganization();
                
            if (org != null) {
                        
                List <OrganizationName> orgNames = org.getOrganizationNames();
                for (OrganizationName name : orgNames) {
                    if (name.getName().getLocalString().toLowerCase().indexOf(currentToken) > -1) {
                        return true;
                    }
                }
                        
                List <OrganizationDisplayName> orgDisplayNames = org.getDisplayNames();
                for (OrganizationDisplayName name : orgDisplayNames) {
                    if (name.getName().getLocalString().toLowerCase().indexOf(currentToken) > -1) {
                        return true;
                    }
                }                                
            }
        }
        return false;
    }

    /**
     * Return all the Idp in the provided entities descriptor.  If SearchMatches
     * is non null it is populated with whatever of the IdPs matches the search string 
     * (as noted above). 
     * @param searchString to match with
     * @param config parameter to mathing
     * @param searchMatches if non null is filled with such of the sites which match the string
     * @return the sites which fit.
     */
    protected Map<String, IdPSite> getIdPSites(String searchString, 
                                               HandlerConfig config, 
                                               Collection<IdPSite> searchMatches)
    {
        XMLObject object;
        List <EntityDescriptor> entities;
        try {
            object = metadata.getMetadata();
        } catch (MetadataProviderException e) {
            LOG.error("Metadata for " + location + "could not be read", e);
            return null;
        }
        
        if (object == null) {
            return null;
        }
        
        //
        // Fill in entities approptiately
        //
        
        if (object instanceof EntityDescriptor) {
            entities = new ArrayList<EntityDescriptor>(1);
            entities.add((EntityDescriptor) object);
        } else if (object instanceof EntitiesDescriptor) {

            EntitiesDescriptor entitiesDescriptor = (EntitiesDescriptor) object; 
    
            entities = getAllEntities(entitiesDescriptor);
        } else {
           return null;
        }
       
        //
        // populate the result (and the searchlist) from the entities list
        //
        
        TreeMap<String, IdPSite> result = new TreeMap <String,IdPSite>();
                    
        for (EntityDescriptor entity : entities) {
                
            if (entity.isValid() && hasIdPRole(entity)) {

                IdPSite site = new IdPSite(entity);
                result.put(site.getName(), site);
                if (searchMatches != null && isMatch(entity, searchString, config)) {           

                    searchMatches.add(site);
                }

            }
        } // iterate over all entities
        return result;
    }


    /**
     * Return this sites (internal) identifier.
     * @return the identifier
     */
    protected String getIdentifier() {
        return identifier;
    }

    /**
     * Return the human friendly name for this siteset.
     * @return The friendly name
     */
    protected String getDisplayName() {
        return displayName;
    }

    /**
     * We do not need to look at a set if it doesn't know about the given SP.  However if
     * no SP is given (as per 1.1) then we do need to look.  This calls lets us know whether 
     * this set is a canddiate for looking into.
     * @param SPName the Sp we are interested in.
     * @return whether the site contains the SP.
     */
    protected boolean containsSP(String SPName) {

        //
        // Deal with the case where we do *not* want to search by
        // SP (also handles the 1.1 case)
        //
        
        if ((SPName == null) || (SPName.length() == 0)) {
            return true;
        }

        //
        // Get hold of the current object list so as to provoke observer to fire 
        // if needs be.
        // 
        
        XMLObject object;
        try {
            object = metadata.getMetadata();
        } catch (MetadataProviderException e) {
            return false;
        }
        //
        // Now lookup
        //

        if (object instanceof EntitiesDescriptor ||
            object instanceof EntityDescriptor) {
            return spNames.contains(SPName);
        } else {
            return false;
        }
    }

    /**
     * For plugin handling we need to know quickly if a metadataset contains the idp.
     * @param IdPName the IdP we are interested in.
     * @return whether the site contains the IdP.
     * 
     */

    protected boolean containsIdP(String IdPName) {
        
        if ((IdPName == null) || (IdPName.length() == 0)) {
            return true;
        }

        //
        // Get hold of the current object list so as to provoke observer to fire 
        // if needs be.
        // 
        
        XMLObject object;
        try {
            object = metadata.getMetadata();
        } catch (MetadataProviderException e) {
            return false;
        }
        if (object instanceof EntitiesDescriptor ||
            object instanceof EntityDescriptor) {
            return idpNames.contains(IdPName);
        } else {
            return false;
        }
    }

    //
    // Now deal with plugins - these are delcared to use but we are
    // responsible for their parameter
    //

    /**
     * Declares a plugin to the siteset.
     * @param plugin what to declare
     */
    protected void addPlugin(Plugin plugin) {

        if (plugins.containsKey(plugin)) {
            return;
        }
        
        PluginMetadataParameter param = plugin.refreshMetadata(metadata);
        
        plugins.put(plugin, param);
    }

    /**
     * Return the parameter that this plugin uses.
     * @param plugin
     * @return teh parameter.
     */
    protected PluginMetadataParameter paramFor(Plugin plugin) {
        return plugins.get(plugin);
    }

    /**
     * Return all the entities below the entities descriptor
     * 
     * @param entitiesDescriptor the entities descriptor 
     * @return
     */
    
    private List<EntityDescriptor> getAllEntities(EntitiesDescriptor entitiesDescriptor)
    {
        List<EntityDescriptor> result = new ArrayList<EntityDescriptor>(entitiesDescriptor.getEntityDescriptors());
        for (EntitiesDescriptor entities : entitiesDescriptor.getEntitiesDescriptors()) {
            result.addAll(getAllEntities(entities));
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.opensaml.saml2.metadata.provider.ObservableMetadataProvider.Observer#onEvent(org.opensaml.saml2.metadata.provider.MetadataProvider)
     */
    
    public void onEvent(MetadataProvider provider) {
        Set<String> spNameSet = new HashSet<String>(0);
        Set<String> idpNameSet = new HashSet<String>(0);

        XMLObject obj; 
        try {
            obj = provider.getMetadata();
        } catch (MetadataProviderException e) {
            LOG.error("Couldn't read metadata for " + location, e);
            return;
        }
        if (obj instanceof EntitiesDescriptor) {
            EntitiesDescriptor entitiesDescriptor = (EntitiesDescriptor) obj;
            
            for (EntityDescriptor entity : getAllEntities(entitiesDescriptor)) {
                if (hasSPRole(entity)) {
                    spNameSet.add(entity.getEntityID());
                }
                if (hasIdPRole(entity)) {
                    idpNameSet.add(entity.getEntityID());
                }
            }
        } else if (obj instanceof EntityDescriptor) {
            EntityDescriptor entity = (EntityDescriptor) obj;
            if (hasSPRole(entity)) {
                spNameSet.add(entity.getEntityID());
            }
            if (hasIdPRole(entity)) {
                idpNameSet.add(entity.getEntityID());
            }
        } else {
            LOG.error("Metadata for " + location + " isn't <EntitiesDescriptor> or <EntityDescriptor>");
            return;
        }
        //
        // Now that we have the new set sorted out commit it in
        //
        this.spNames = spNameSet;
        this.idpNames = idpNameSet;
        
        for (Plugin plugin:plugins.keySet()) {
            plugins.put(plugin, plugin.refreshMetadata(provider));
        }
    }

    /**
     * Enumerate all the roles and see whether this entity can be an IdP.
     * @param entity
     * @return true if one of the roles that entity has is IdPSSO
     */
    private static boolean hasIdPRole(EntityDescriptor entity) {
        List<RoleDescriptor> roles = entity.getRoleDescriptors();
        
        for (RoleDescriptor role:roles) {
           if (role instanceof IDPSSODescriptor) {
               //
               // So the entity knows how to be some sort of an Idp
               //
               return true;            
           }
        }
        return false;
    }

    /**
     * Enumerate all the roles and see whether this entity can be an SP.
     * @param entity
     * @return true if one of the roles that entity has is SPSSO
     */
    private static boolean hasSPRole(EntityDescriptor entity) {
        List<RoleDescriptor> roles = entity.getRoleDescriptors();
        
        for (RoleDescriptor role:roles) {
           if (role instanceof SPSSODescriptor) {
               //
               // "I can do that"
               //
               return true;
           }
        }
        return false;
    }

    /**
     * Return the idpSite for the given entity name.
     * @param idpName the entityname to look up
     * @return the associated idpSite
     * @throws WayfException
     */
    protected IdPSite getSite(String idpName) throws WayfException {

        try {
            return new IdPSite(metadata.getEntityDescriptor(idpName));
        } catch (MetadataProviderException e) {
            String s = "Couldn't resolve " + idpName + " in "  + getDisplayName();
            LOG.error(s, e);
            throw new WayfException(s, e);
        }
    }
    
    protected EntityDescriptor getEntity(String name) throws WayfException {
        try {
            return metadata.getEntityDescriptor(name);
        } catch (MetadataProviderException e) {
            String s = "Couldn't resolve " + name + " in "  + getDisplayName();
            LOG.error(s, e);
            throw new WayfException(s, e);
        }
        
    }
}

