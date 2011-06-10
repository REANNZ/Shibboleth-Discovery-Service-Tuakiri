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
 * Signals that an error has occurred while processing a Shibboleth WAYF request.
 * 
 * @author Walter Hoehn wassa&#064;columbia.edu
 */

public class WayfException extends Exception {

    /** Required serialization constant. */ 
    private static final long serialVersionUID = 8426660801169338914L;
    
    /**
     * Constructor with a description and an exception.
     * @param s description
     * @param e something bad having happened.
     */
    public WayfException(String s, Throwable e) {
        super(s, e);
    }

    /**
     * Constructure with just a description.
     * @param s description
     */
    public WayfException(String s) {
        super(s);
    }
}
