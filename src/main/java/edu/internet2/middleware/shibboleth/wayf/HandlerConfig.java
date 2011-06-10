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

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.shibboleth.common.ShibbolethConfigurationException;

/**
 * Class used by the DiscoveryServiceHandler to handle run time behaviour. 
 */

public class HandlerConfig {

    /**
     * How to get debug output out.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HandlerConfig.class.getName());
    
    /** A set of names which are ignored when doing a search. */
    private final HashSet <String> ignoredForMatch;
        
    /** Where to find the GUI description jsp file. */
    private final String jspFile;
    
    /** Where to find the error jsp file. */
    private final String errorJspFile;
        
    /** Do we give the jsp file an array of arrays of IdPs? */
    private final boolean provideListOfLists;
        
    /** Do we give the jsp file a flat list of IdPs? */
    private final boolean provideList;
    
    /** Do we do a pre-filter by SP name in for each metadata provider. */
    private final boolean lookupSp; 
    
    /** Do we warn on the bad binding. */
    private final boolean warnOnBadBinding;
 
    /** Build the 'default default' configuation. */ 
    public HandlerConfig() {
        //
        // 'Sensible' default values
        //
        jspFile = "/wayf.jsp";
        errorJspFile = "/wayfError.jsp";
        provideList = true;
        provideListOfLists = false;
        lookupSp = true;
        ignoredForMatch = new HashSet <String>(); 
        warnOnBadBinding = false;
    }
        
        
    /**
     * 
     * Parse the Supplied XML element into a new WayfConfig Object.
     * @param config - The XML with the configuration info.
     * @param defaultValue - The default if nothing is specified.
     * @throws ShibbolethConfigurationException - if we see somethin wrong.
     */
    public HandlerConfig(Element config, HandlerConfig defaultValue) throws ShibbolethConfigurationException {
        
        String attribute;   
        LOG.debug("Loading global configuration properties.");

        NodeList list = config.getElementsByTagName("SearchIgnore");
        
        if (list.getLength() == 0) {
            
            ignoredForMatch = defaultValue.ignoredForMatch;

        } else { 
            
            ignoredForMatch = new HashSet<String>();        
                
            for (int i = 0; i < list.getLength(); i++ ) {
                    
                    NodeList inner = ((Element) list.item(i)).getElementsByTagName("IgnoreText");
                    
                    for(int j = 0; j < inner.getLength(); j++) {
                            
                            addIgnoredForMatch(inner.item(j).getTextContent());
                    }
                }
        }

        attribute = config.getAttribute("jspFile");
        if (attribute != null && !attribute.equals("")) {
                jspFile = attribute;
        } else {
                jspFile = defaultValue.jspFile;
        }
        
        attribute = config.getAttribute("errorJspFile");
        if (attribute != null && !attribute.equals("")) {
                errorJspFile = attribute;
        } else {
                errorJspFile = defaultValue.errorJspFile;
        }
        
        attribute = config.getAttribute("provideList");
        if (attribute != null && !attribute.equals("")) {
                provideList = Boolean.valueOf(attribute).booleanValue();
        } else { 
                provideList = defaultValue.provideList;
        }

        attribute = config.getAttribute("provideListOfList");
        if (attribute != null && !attribute.equals("")) {
                provideListOfLists = Boolean.valueOf(attribute).booleanValue();
        } else {
                provideListOfLists = defaultValue.provideListOfLists;
        }
        
        attribute = config.getAttribute("showUnusableIdPs");
        if (attribute != null && !attribute.equals("")) {
                lookupSp = !Boolean.valueOf(attribute).booleanValue();
        } else {
                lookupSp = defaultValue.lookupSp;
        }
        
        attribute = config.getAttribute("warnOnBadBinding");
        if (null != attribute && !attribute.equals("")) {
                warnOnBadBinding = Boolean.valueOf(attribute).booleanValue();
        } else {
            warnOnBadBinding = false;
        }
    }
    

    /**
     * Determines if a particular string token should be used for matching when a user searches for origins.
     * 
     * @param str - The string to lookup.
     * @return whether it is or not.
     */
    public boolean isIgnoredForMatch(String str) {

        return ignoredForMatch.contains(str.toLowerCase());
    }

    /**
     * Sets the tokens that should be ignored when a user searches for an origin site.
     * 
     * @param s
     *            The ignored tokens are passed as a single string, each separated by whitespace
     */
    private void addIgnoredForMatch(String s) {

            ignoredForMatch.add(s.toLowerCase());
    }

    /**
     * Get the name of the jsp File this instance uses.
     * @return the name.
     */
    public String getJspFile() {
            return jspFile;
    }
    
    /**
     * Get the name of the error jsp File this instance uses.
     * @return the name.
     */
    public String getErrorJspFile() {
            return errorJspFile;
    }
    
    /**
     * Do we provide a list of lists of IdPs?.
     * @return whether we do or not.
     */
    public boolean getProvideListOfLists() {
            return provideListOfLists;
    }
    
    /**
     * Do we provide a list of IdPs?.
     * @return whether we provide a list of IdPs?.
     */
    public boolean getProvideList() {
        return provideList;
    }
    
    /**
     * Do we lookup the SP or just return all the IdPs?.
     * @return whether or not we lookup the SP
     */
    public boolean getLookupSp() {  
        return lookupSp;  
    }
    
    /**
     * Do ignore badly formed bindings or just warn
     * @return whether we warn.
     */
    public boolean getWarnOnBadBinding() {  
        return warnOnBadBinding;  
    }
    
}
