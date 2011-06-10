/**
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
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
package edu.internet2.middleware.shibboleth.wayf.plugins;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.saml2.metadata.provider.MetadataProvider;

import edu.internet2.middleware.shibboleth.wayf.IdPSite;

/**
 * 
 * The Plugin interface is used to affect the 'hints' that the WAYF offers to the users.  
 * <p>
 * The WAYF can register any number of plugins.  Each plugin is called when the metadata is loaded or realoaded
 * (so it can do any indexing) and at the three entry points into the WAYF - Lookup (main entry), Search and Select.
 * Plugins are called in the order in which they are declared to the WAYF. 
 * <p>
 * Each plugin is called once when the user has made a selection.
 * <p>
 * For Search and Lookup, each plugin is called multiple times, once for each metadata provider which has 
 * been declared to this particular WAYF instance.  The plugin can return a context which is handed to subsequent calls.
 * <p>
 * The idea is that each plugin can affect the displayed lists of IdPs.  As a reminder the WAYF displays two lists of 
 * IdPs - the complete list, displayed either as a single list or a list of lists, and the hint list (which was
 * previously only populated from the _saml_idp cookie.  In the search case the WAYF displays a third list of
 * the search hits. 
 * <p>
 * When the plugin in called it is given the current set of potential IdPs as a Map from EntityID to {@link IdPSite}
 * and lists representing the current hint list and search results.  A Plugin can remove an entry from 
 * the map or the lists.  Additionally it can insert an IdPSite found in the Map into the hint or search lists.
 * Thus the plugin can restrict the number of sites that the WAYF instance displays in the 'complete list' and
 * can add or remove IdPs from the hint list.
 * <p>
 * At any stage the plugin can take control of the current request and redirect or forward it.  It signals that
 * it has done this to the WAYF by means of an exception.
 * <p> 
 *  The _saml_idp cookie handling code is written as a WAYF plugin.  Other plugins have been written to allow IdPs
 *  to be presented as hints based on the client's IP address or to redirect back to the SP once the choice of
 *  IdP has been made.
 *  <p>
 *  Object implementing this interface are created during WAYF discovery service initialization.  There are 
 *  expected to implement a constructor which takes a {@link org.w3c.dom.Element} as the only parameter and they are 
 *  created via this constructor, with the parameter being the appropriate section of the WAYF configuration file 
 *  
 * @version Discussion
 *
 */
public interface Plugin {

        /**
         *  Whenever the WAYF discoveres that the metadata is stale, it reloads it and calls each plugin at this method.
         * 
         * @param metadata - where to get the data from.
         * @return the value which will be provided as input to subsequent calls to {@link #lookup Lookup} and 
         * {@link #search Search}
         */
        PluginMetadataParameter refreshMetadata(MetadataProvider metadata);
        
        /**
         * The WAYF calls each plugin at this entry point when it is first contacted.  
         * 
         * @param req - Describes the current request.  A Plugin might use it to find any appropriate cookies 
         * @param res - Describes the current response.  A Plugin might use it to redirect a the request. 
         * @param parameter Describes the metadata.
         * @param context Any processing context returned from a previous call.
         * @param validIdps The list of IdPs which is currently views as possibly matches for the pattern. 
         *                  The Key is the EntityId for the IdP and the value the object which describes 
         *                  the Idp 
         * @param idpList The set of Idps which are currently considered as potential hints.    
         * @return a context to hand to subsequent calls
         * @throws WayfRequestHandled if the plugin has handled the request (for instance it has
         * issues a redirect)
         *
         * Each plugin is called multiple times,
         * once for each metadata provider which is registered (Depending on the precise configuration of the WAYF
         * metadata providers whose metadata does not include the target may be dropped).  Initially the plugin is
         * called with a context parameter of <code>null</code>.  In subsequent calls, the value returned from
         * the previous call is passed in as the context parameter. 
         * 
         * The plugin may remove IdPSite objects from the validIdps list.
         * 
         * The plugin may add or remove them to the idpList.  IdPSite Objects which are to be added to the idpList 
         * should be looked up by EntityIdName in validIdps by EntityId.  Hence any metadata processing shoudl 
         * store the entityID. 
         * 
         */
        PluginContext lookup(HttpServletRequest req, 
                                                 HttpServletResponse res, 
                                                 PluginMetadataParameter parameter, 
                                                 Map<String, IdPSite> validIdps, 
                                                 PluginContext context, 
                                                 List<IdPSite> idpList) throws WayfRequestHandled;

        /**
         * This method is called when the user specified a search operation.  The processing is similar to 
         * that described for {@link #lookup lookup}.
         * Two additional paramaters are provided, the search parameter which was provided, and the current 
         * proposed list of candidate IdPs.  The plugin is at liberty to alter both the list of hints and the 
         * list of valid IdPs. 
         * 
         * @param req Describes the current request.  The Plugin could use it to find any appropriate cookies 
         * @param res Describes the result - this is needed if (for instance) a plung needs to change cookie values
         * @param parameter Describes the metadata
         * @param pattern The Search pattern provided
         * @param validIdps The list of IdPs which is currently views as possibly matches for the pattern.  
         *                  The Key is the Idp Name an the value the idp
         * @param context Any processing context returned from a previous call.
         * @param searchResult the resukt of any search
         * @param idpList The set of Idps which are currently considered as potential hints.  Each Idp is associated
         * with a numeric weight, where the lower the number is the more likely the IdP is to be a candidate.  
         * As descibed above the WAYF uses this to provide hint list to the GUI (or even to dispatch 
         * immediately to the IdP).  
         * @return a context to hand to subsequent calls
         * @throws WayfRequestHandled if the plugin has handled the request (for instance it has
         * issues a redirect)
         */
        PluginContext search(HttpServletRequest req, 
                                        HttpServletResponse res, 
                                        PluginMetadataParameter parameter, 
                                        String pattern, 
                                        Map<String, IdPSite> validIdps, 
                                        PluginContext context, 
                                        Collection<IdPSite> searchResult,
                                        List<IdPSite> idpList) throws WayfRequestHandled;
        
        /**
         * This method is called, for every plugin, after a user has selected an IdP.  The plugin is expected 
         * to use it to update any in memory state (via the {@link PluginMetadataParameter} parameter or permananent 
         * state (for instance by writing back a cookie.
         * @param req Describes the current request. 
         * @param res Describes the current response
         * @param parameter  Describes the metadata
         * @throws WayfRequestHandled if the plugin has handled the request (for instance it has
         * issues a redirect)
         */
        void selected(HttpServletRequest req, 
                      HttpServletResponse res, 
                      PluginMetadataParameter parameter, 
                      String idP) throws WayfRequestHandled;
}
