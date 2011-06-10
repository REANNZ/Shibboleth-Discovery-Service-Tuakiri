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

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.LocalizedString;
import org.opensaml.xml.LangBearing;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;

/**
 * A thread-safe unmarshaller for {@link edu.internet2.middleware.shibboleth.wayf.idpdisco.LocalURI} objects.
 */
public class LocalizedURIUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /**
     * {@inheritDoc}
     */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        if (attribute.getLocalName().equals(LangBearing.XML_LANG_ATTR_LOCAL_NAME)
                && SAMLConstants.XML_NS.equals(attribute.getNamespaceURI())) {
            LocalizedURI uri = (LocalizedURI) samlObject;

            LocalizedString uriStr = uri.getURI();
            if (uriStr == null) {
                uriStr = new LocalizedString();
            }

            uriStr.setLanguage(attribute.getValue());
            uri.setURI(uriStr);
        }
    }

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        LocalizedURI uri = (LocalizedURI) samlObject;

        LocalizedString uriStr = uri.getURI();
        if (uriStr == null) {
            uriStr = new LocalizedString();
        }

        uriStr.setLocalizedString(elementContent);
        uri.setURI(uriStr);
    }
}