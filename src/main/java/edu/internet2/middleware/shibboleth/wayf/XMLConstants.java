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

/**
 * Holder class for various XML constants (namespaces and so forth).
 */
public class XMLConstants {

    /** Shibboleth XML namespace. */
    public static final String SHIB_NS = "urn:mace:shibboleth:1.0";

    /** SSO Binding name. */
    public static final String IDP_SSO_BINDING = "urn:mace:shibboleth:1.0:profiles:AuthnRequest";

    /** Namespace for Discovery Service configuration. */
    public static final String CONFIG_NS = "urn:mace:shibboleth:wayf:config:1.0";
    
    /** SAML2 protocol */
    public static final String SALM2_PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";
    
}
