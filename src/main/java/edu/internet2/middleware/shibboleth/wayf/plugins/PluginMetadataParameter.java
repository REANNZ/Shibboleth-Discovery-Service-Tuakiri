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


/**
 * The PluginParameter is a marker interface which a WAYF {@link Plugin} can use to associate extra information 
 * with the metadata.  Each plugin returns a PluginParameter from {@link Plugin#refreshMetadata RefreshMetadata}
 * and this is in turn presented back to then Plugin when it is called  during WAYF processing. 
 * <p>
 * @author Rod Widdowson
 * @version Discussion
 *
 */
public interface PluginMetadataParameter {

}
