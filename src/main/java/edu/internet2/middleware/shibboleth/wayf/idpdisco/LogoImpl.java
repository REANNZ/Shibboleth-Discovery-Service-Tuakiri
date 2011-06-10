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

import java.util.List;

import org.opensaml.common.impl.AbstractSAMLObject;
import org.opensaml.saml2.metadata.LocalizedString;
import org.opensaml.xml.XMLObject;

/**
 * Concrete implementation of {@link edu.internet2.middleware.shibboleth.wayf.idpdisco.Logo}.
 * @author rod widdowson
 */
public class LogoImpl extends AbstractSAMLObject implements Logo {
    
    /** Logo URL. */
    private LocalizedString localizedURL;

    /** X-Dimension of the logo. */
    private Integer width;

    /** Y-Dimension of the logo. */
    private Integer height;

    /**
     * Constructor.
     * 
     * @param namespaceURI namespaceURI
     * @param elementLocalName elementLocalName
     * @param namespacePrefix namespacePrefix
     */
    protected LogoImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }


    /** {@inheritDoc} */
    public Integer getHeight() {
        return height;
    }

    /** {@inheritDoc} */
    public void setHeight(Integer newHeight) {
         height = prepareForAssignment(height, newHeight);
    }

    /** {@inheritDoc} */
    public Integer getWidth() {
        return width;
    }

    /** {@inheritDoc} */
    public void setWidth(Integer newWidth) {
        width = prepareForAssignment(width, newWidth);
    }

    /** {@inheritDoc} */
    public LocalizedString getURL() {
        return localizedURL;
    }

    /** {@inheritDoc} */
    public void setURL(LocalizedString newURL) {
        localizedURL = newURL;
    }

    /** {@inheritDoc} */
    public String getXMLLang() {
        localizedURL.getLanguage();
        return null;
    }


    /** {@inheritDoc} */
    public void setXMLLang(String newLang) {
        localizedURL.setLanguage(newLang);
    }

    /** {@inheritDoc} */
    public List<XMLObject> getOrderedChildren() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + localizedURL.hashCode();
        hash = hash * 31 + height;
        hash = hash * 31 + width;
        return hash;
    }
}
