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

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.Organization;
import org.opensaml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml2.metadata.SingleSignOnService;

/**
 * A class which abstracts an IdP for the sake of the WAYF display.  Given an {@link EntityDescriptor} as
 * input it provides bean style get functions for the name (EntityId), the display name 
 * (a hybrid of Organization name or EntityId and the IdP's SSO connection point.
 * 
 */
public class IdPSite {

    /** The OpenSaml element that this stands for. */
    private EntityDescriptor entity;
    
    /** The language we set up */
    private String displayLanguage;
    
    /**
     * Create a new element from the provided Entity.
     * @param entityParam - What to create from
     */
    public IdPSite(EntityDescriptor entityParam) {
        entity = entityParam;
    }
    
    /**
     * Get the name for the enclosed entity. 
     * @return the name for the enclosed entity.
     */
    public String getName() {
            return entity.getEntityID();
    }
    
    /**
     * Get the user friendly name for the entity, collecting the locale from the 
     * browser if possible
     * @param req the request
     * @return a user friendly name.
     */
    public String getDisplayName(HttpServletRequest req) {
        //
        // Get the browser locale, failing that the server one
        //
        Locale locale = req.getLocale();
        if (null == locale) {
            Locale.getDefault();
        }
        String lang = locale.getLanguage();
            
        return getDisplayName(lang);
    }
    /**
     * Get the user friendly name for the entity, using provided language
     * @param lang the language.
     * 
     * @return a user friendly name.
     */
    private String getDisplayName(String lang) {
        Organization org = entity.getOrganization();
    
        if (org == null) {
            return entity.getEntityID();
        } 
        
        List<OrganizationDisplayName> list = org.getDisplayNames();

        //
        // Lookup first by locale
        //
        
        for (OrganizationDisplayName name:list) {
            if (null !=name && lang.equals(name.getName().getLanguage())) {
                return name.getName().getLocalString();
            }
        }
        
        //
        // If that doesn't work then anything goes
        //
        
        for (OrganizationDisplayName name:list) {
            if (null !=name && null != name.getName().getLocalString()) {
                return name.getName().getLocalString();
            }
        }
     
        //
        // If there is still nothing then use the entity Id
        //
        return entity.getEntityID();
    }
    /**
     * Get the user friendly name for the entity, the language we previously set up.
     * 
     * @return a user friendly name.
     */
    public String getDisplayName() {
        return getDisplayName(displayLanguage);
    }
    
    /**
     * Return all the extension elements.
     * @return the extensions
     */
    public Extensions getExtensions() {
        IDPSSODescriptor idpss = entity.getIDPSSODescriptor(XMLConstants.SHIB_NS);
        if (null == idpss) {
            //
            // Get the SAML2 protocol
            //
            idpss = entity.getIDPSSODescriptor(XMLConstants.SALM2_PROTOCOL);
        }
        if (null == idpss) {
            return null;
        }
        return idpss.getExtensions();
    }
    
    /**
     * Comparison so we can sort the output for jsp.
     * @param What to compare against
     * @return numeric encoding of comparison 
     * @see java.lang.Comparator
     */
    protected int compareTo(Object o, HttpServletRequest req) {
            

        String myDisplayName;
        String otherDisplayName;
        IdPSite other;

        if (equals(o)) {
            return 0;
        }

        myDisplayName = getDisplayName(req);
        if (null == myDisplayName) {
            myDisplayName = "";
        } 
        
        other = (IdPSite) o;
        otherDisplayName = other.getDisplayName(req);
        if (null == otherDisplayName) {
            otherDisplayName = "";
        }

        int result = myDisplayName.toLowerCase().compareTo(otherDisplayName.toLowerCase());
        if (result == 0) {
                result = myDisplayName.compareTo(otherDisplayName);
        }
        return result;
    }

    /**
     * When a user has selected an IdP, this provides the address to which we redirect.
     * @return http address for the IdP this represents.  
     */
    public String getAddressForWAYF() {
        List<SingleSignOnService> ssoList;

        IDPSSODescriptor idpssoDesc = entity.getIDPSSODescriptor(XMLConstants.SHIB_NS);
        if (idpssoDesc == null) { return null; };
        
        ssoList = idpssoDesc.getSingleSignOnServices();
        
        for (SingleSignOnService signOnService: ssoList) {
            if (XMLConstants.IDP_SSO_BINDING.equals(signOnService.getBinding())) {
                return signOnService.getLocation();
            }
        }
        return null;
    }

    /**
     * Prior to display we set the display language from the
     * browser. There is probably a proper way to do this using
     * jsp, but I want to keep the API between JSP and java the same 1.3->2.0
     * @param lang the language to set
     */
    public void setDisplayLanguage(String lang) {
        this.displayLanguage = lang;
    }
    
    public static class Compare implements Comparator<IdPSite> {

        /**
         * This allows us to set up sorted lists of entities with respect to
         * the browser request.
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        private HttpServletRequest req = null;
        
        private Compare() {
            //
            // No public method
        }
        
        public Compare(HttpServletRequest req) {
            this.req = req;
        }
        
        public int compare(IdPSite o1, IdPSite o2) {
            // TODO Auto-generated method stub
            return o1.compareTo(o2, req);
        }
        
    }

}       

