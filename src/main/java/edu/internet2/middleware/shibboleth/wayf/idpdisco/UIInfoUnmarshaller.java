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
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.metadata.UIInfo} objects.
 */
public class UIInfoUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {
        UIInfo info = (UIInfo) parentSAMLObject;

        if (childSAMLObject instanceof Description) {
            info.getDescriptions().add((Description) childSAMLObject);
        } else if (childSAMLObject instanceof DisplayName) {
            info.getDisplayNames().add((DisplayName) childSAMLObject);
        } else if (childSAMLObject instanceof InformationURL) {
            info.getInformationURLs().add((InformationURL) childSAMLObject);
        } else if (childSAMLObject instanceof Logo) {
            info.getLogos().add((Logo) childSAMLObject);
        } else if (childSAMLObject instanceof PrivacyStatementURL) {
            info.getPrivacyStatementURLs().add((PrivacyStatementURL) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }
}