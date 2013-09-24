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
    private static boolean messageIsCheckedHTML = false;

    /**
     * Return the flag whether the message is HTML that has been checked for user supplied content and can be safely sent back to the browser
     * @return the value of the messageIsCheckedHTML flag
     */
    public boolean getMessageIsCheckedHTML() {
        return messageIsCheckedHTML;
    }
    
    /**
     * Constructor with a description and an exception.
     * @param s description
     * @param e something bad having happened.
     */
    public WayfException(String s, Throwable e) {
        super(s, e);
    }

    /**
     * Constructor with just a description.
     * @param s description
     */
    public WayfException(String s) {
        super(s);
    }
    
    /**
     * Constructor with a description, a boolean messageIsCheckedHTML flag and an exception.
     * @param s description
     * @param messageIsCheckedHTML Is the message HTML that has been checked for user supplied content and can be safely sent back to the browser
     * @param e something bad having happened.
     */
    public WayfException(String s, boolean messageIsCheckedHTML, Throwable e) {
        super(s, e);
        this.messageIsCheckedHTML = messageIsCheckedHTML;
    }

    /**
     * Constructor with a description and boolean messageIsCheckedHTML flag.
     * @param s description
     * @param messageIsCheckedHTML Is the message HTML that has been checked for user supplied content and can be safely sent back to the browser
     */
    public WayfException(String s, boolean messageIsCheckedHTML) {
        super(s);
        this.messageIsCheckedHTML = messageIsCheckedHTML;
    }
}
