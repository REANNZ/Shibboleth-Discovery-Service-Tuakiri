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

import java.util.Collection;
/**
 * This is just a container class for tieing together a set of IdPs to a name - this being what
 * is sent to the JSP for display purposes.
 */
public class IdPSiteSetEntry {
        
    /** The metadata provider. */
    private final IdPSiteSet siteSet;
    
    /** The IdPs associated with that metadata provider. */
    private final Collection<IdPSite> sites;
    
    /**
     * Create an object which contains just these two objects.
     * @param siteSetParam the metadata provider.
     * @param sitesParam the list of IdPs. 
     */
    public IdPSiteSetEntry(IdPSiteSet siteSetParam, Collection<IdPSite> sitesParam) {
        this.siteSet = siteSetParam;
        this.sites = sitesParam;
    }
    
    /** 
     * Return something to display for this set of sites. 
     * @return the name as defined in the configuration
     */
    public String getName() {
        return siteSet.getDisplayName();
    }
    
    /**
     * Return the list of associated sites. 
     * @return a collection of IdPs.
     */
    public Collection<IdPSite> getSites() {
        return sites;
    }

}
