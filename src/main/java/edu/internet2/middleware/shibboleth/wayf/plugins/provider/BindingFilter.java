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

import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.FilterException;
import org.opensaml.saml2.metadata.provider.MetadataFilter;
import org.opensaml.samlext.idpdisco.DiscoveryResponse;
import org.opensaml.xml.XMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See SDSJ-48.  If we get a DS endpoint then we need to check that the binding is provided
 * and that it is correct.
 * 
 * @author Rod Widdowson
 *
 */
public class BindingFilter implements MetadataFilter {

    /**
     * Log for the warning. 
     */
    private static final Logger LOG = LoggerFactory.getLogger(BindingFilter.class.getName());
    
    /**
     * Set if we just want to warn on failure.
     */
    private final boolean warnOnFailure;
    
    /**
     * Only the protected constructor should be visible.
     */
    private BindingFilter() {
        this.warnOnFailure = false;
    }
    
    /**
     * Initialize the filter.
     * @param warn do we warn or do we fail if we see badness?
     */
    public BindingFilter(boolean warn) {
        this.warnOnFailure = warn;
    }
    
    /**
     * Apply the filter.
     * @see org.opensaml.saml2.metadata.provider.MetadataFilter#doFilter(org.opensaml.xml.XMLObject)
     * @param metadata what to filter.
     * @throws org.opensaml.saml2.metadata.provider.FilterException if it sees any missed or bad bindings.
     */
    public void doFilter(XMLObject metadata) throws FilterException {

        if (metadata instanceof EntitiesDescriptor) {
            
            checkEntities((EntitiesDescriptor) metadata);
            
        } else if (metadata instanceof EntityDescriptor) {
            EntityDescriptor entity = (EntityDescriptor) metadata;
            
            if (!checkEntity(entity)) {
                if (warnOnFailure) {
                    LOG.warn("Badly formatted binding for " + entity.getEntityID());
                } else {
                    LOG.error("Badly formatted binding for top level entity " + entity.getEntityID());
                }
            }
        }
    }

    /**
     * If the entity has an SP characteristic, and it has a DS endpoint
     * then check its binding.
     * 
     * @param entity what to check.
     * @return true if all is OK.
     */
    private static boolean checkEntity(EntityDescriptor entity) {
        List<RoleDescriptor> roles = entity.getRoleDescriptors();
        
        for (RoleDescriptor role:roles) {
            
            //
            // Check every role
            //
            if (role instanceof SPSSODescriptor) {
                
                //
                // Grab hold of all the extensions for SPSSO descriptors
                //
                
                Extensions exts = role.getExtensions();
                if (exts != null) {
                    //
                    // We have some children check them form <DiscoveryResponse>
                    //
                    List<XMLObject> children = exts.getOrderedChildren();
                    
                    for (XMLObject obj : children) {
                        if (obj instanceof DiscoveryResponse) {
                            //
                            // And check or the binding
                            //
                            DiscoveryResponse ds = (DiscoveryResponse) obj;
                            String binding = ds.getBinding(); 

                            if (!DiscoveryResponse.IDP_DISCO_NS.equals(binding)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Check an EntitiesDescriptor call checkentities for the Entities and ourselves
     *  recursively for the EntitesDescriptors.
     *  
     * @param entities what to check.
     */
    private void checkEntities(EntitiesDescriptor entities) {
        List<EntitiesDescriptor> childEntities = entities.getEntitiesDescriptors();
        List<EntityDescriptor> children = entities.getEntityDescriptors();
        Collection<EntityDescriptor> excludes = new HashSet<EntityDescriptor>();
        
        if (children != null) {
            Iterator<EntityDescriptor> itr;
            EntityDescriptor entity;
            itr = children.iterator();
            
            while (itr.hasNext()) {
                entity = itr.next();
                if (!checkEntity(entity)) { 
                    if (warnOnFailure) {
                        LOG.warn("Badly formatted binding for " + entity.getEntityID());
                    } else {
                        LOG.error("Badly formatted binding for " + entity.getEntityID() + ". Entity has been removed");
                        excludes.add(entity);
                    }
                }
            }
            children.removeAll(excludes);
        }
        
        if (childEntities != null) {
            for (EntitiesDescriptor descriptor : childEntities) {
                checkEntities(descriptor);
            }
        }
    }
}
