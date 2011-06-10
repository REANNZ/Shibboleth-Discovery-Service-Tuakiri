/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
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

package edu.internet2.middleware.shibboleth.wayf.idpdisco;

import java.util.ArrayList;
import java.util.List;

import org.opensaml.common.impl.AbstractSAMLObject;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.util.XMLObjectChildrenList;

/**
 *  Concrete implementation of {@link edu.internet2.middleware.shibboleth.wayf.idpdisco.DiscHints}.
 * @author Rod Widdowson
 *
 */
public class DiscoHintsImpl extends AbstractSAMLObject implements DiscoHints {

    /** DNS Domain hints. */
    private final XMLObjectChildrenList<DomainHint> domainHints;
    
    /** IP Address hints. */
    private final XMLObjectChildrenList<IPHint> iPHints;

    /** GeoLocation hints. */
    private final XMLObjectChildrenList<GeolocationHint> geoHints;
    
    /**
     * Constructor.
     * @param namespaceURI namespaceURI
     * @param elementLocalName elementLocalName
     * @param namespacePrefix namespacePrefix
     */
    
    protected DiscoHintsImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
        domainHints = new XMLObjectChildrenList<DomainHint>(this);
        iPHints = new XMLObjectChildrenList<IPHint>(this);
        geoHints = new XMLObjectChildrenList<GeolocationHint>(this);
    }

    /** {@inheritDoc} */
    public List<DomainHint> getDomainHints() {
        return domainHints;
    }

    /** {@inheritDoc} */
    public List<GeolocationHint> getGeolocationHints() {
        return geoHints;
    }

    /** {@inheritDoc} */
    public List<IPHint> getIPHints() {
        return iPHints;
    }

    /** {@inheritDoc} */
    public List<XMLObject> getOrderedChildren() {
        ArrayList<XMLObject> children = new ArrayList<XMLObject>();
        
        children.addAll(domainHints);
        children.addAll(iPHints);
        children.addAll(geoHints);
        return children;
    }
}
