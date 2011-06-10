/*
 * Copyright 2008 University Corporation for Advanced Internet Development, Inc.
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

package edu.internet2.middleware.shibboleth.wayf.plugins.provider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.provider.FilterException;
import org.opensaml.saml2.metadata.provider.MetadataFilter;
import org.opensaml.xml.XMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.shibboleth.wayf.XMLConstants;

/**
 * See SDSJ-57.  Explicit 
 * 
 * @author Rod Widdowson
 *
 */
public class ListFilter implements MetadataFilter {

    /**
     * Log for any messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ListFilter.class.getName());
    
    /**
     * Set if this is a blacklist.
     */
    private boolean excludeEntries;
    
    /**
     * The list of entities.
     */
    private final Set<String> filterEntities;
    
    /**
     * The name of the filter (needed for debug).
     */
    private final String filterName;
    
    /**
     * Only the protected constructor should be visible.
     */
    private ListFilter() {
        this.excludeEntries = false;
        this.filterEntities = new HashSet<String>(0);
        this.filterName = "anonymous";
    }
    
    /**
     * Initialize the filter.
     * @param config the configuration
     *
     * The configuration looks liken this
     * <code> <Filter identifier="WhiteList" 
     *                type ="edu.internet2.middleware.shibboleth.wayf.plugins.provider.ListFilter"
     *                excludeEntries = "true" >
     *        <EntityId>foo</EntityId>
     *        [...]
     *        </Filter>
     *  </code>
     */
    public ListFilter(Element config) {
        String excludeEntriesValue;
        this.filterEntities = new HashSet<String>(10);
        this.filterName = config.getAttribute("identifier");
        excludeEntriesValue = config.getAttribute("excludeEntries");
        
        if (null == excludeEntriesValue || 0 == excludeEntriesValue.length()) {
            this.excludeEntries = true;
        } else {
            this.excludeEntries = Boolean.parseBoolean(excludeEntriesValue);
        }
        
        NodeList itemElements = config.getElementsByTagNameNS(XMLConstants.CONFIG_NS, "EntityId");
        
        if (excludeEntries) {
            LOG.debug("Populating blacklist " + filterName);
        } else {
            LOG.debug("Populating whitelist " + filterName);
        }  
        
        for (int i = 0; i < itemElements.getLength(); i++) {
            Element element = (Element) itemElements.item(i);
            String entityId = element.getTextContent();
            
            LOG.debug("\t" + entityId);
            this.filterEntities.add(entityId);
        }
    }
    
    /**
     * Apply the filter.
     * @see org.opensaml.saml2.metadata.provider.MetadataFilter#doFilter(org.opensaml.xml.XMLObject)
     * @param metadata what to filter.
     * @throws FilterException if it sees any missed or bad bindings.
     */
    public void doFilter(XMLObject metadata) throws FilterException {

        if (metadata instanceof EntitiesDescriptor) { 
            filterEntities((EntitiesDescriptor)metadata);
        } else if (metadata instanceof EntityDescriptor) {
            EntityDescriptor entity = (EntityDescriptor) metadata;
            String entityName = entity.getEntityID();
            
            if (excludeEntries) {
                if (filterEntities.contains(entityName)) {
                    LOG.error("Metadata provider contains a single <EntityDescriptor> (" + entityName + 
                              ") which is in exclude list");
                }
            } else if (!filterEntities.contains(entity.getEntityID())) {
                LOG.error("Metadata provider contains a single <EntityDescriptor>  (" + entityName + 
                          ") which is not on include list");
            }
        }
    }

    /**
     * Filter an EntitiesDescriptor .  We do this explictly for the Entities and call ourselves
     *  recursively for the EntitesDescriptors.
     *  
     * @param entities what to check.
     */
    private void filterEntities(EntitiesDescriptor entities) {
        String entitiesName = entities.getName();
        List<EntitiesDescriptor> childEntities = entities.getEntitiesDescriptors();
        List<EntityDescriptor> children = entities.getEntityDescriptors();
        Collection<EntityDescriptor> excludes = new HashSet<EntityDescriptor>();
        
        //
        // Go through and apply the filter
        //

        if (children != null) {
            Iterator<EntityDescriptor> itr;
            EntityDescriptor entity;
            itr = children.iterator();
            
            while (itr.hasNext()) {
                entity = itr.next();
                String entityName = entity.getEntityID();
                if (excludeEntries) {

                    if (filterEntities.contains(entityName)) {
                        LOG.debug("Filter " + filterName + ": Removing blacklisted "  
                                + entityName + " from " + entitiesName);
                        excludes.add(entity);
                    }
                } else if (!filterEntities.contains(entityName)) {
                    LOG.debug("Filter " + filterName + ": Removing non-whitelisted "  
                            + entityName + " from " + entitiesName);

                    excludes.add(entity);
                }
            } 
            children.removeAll(excludes);
        }
        
        if (childEntities != null) {
            for (EntitiesDescriptor descriptor : childEntities) {
                filterEntities(descriptor);
            }
        }
    }
}
