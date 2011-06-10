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

package edu.internet2.middleware.shibboleth.common;

/**
 * Signals that the a Shibboleth component has been given insufficient or improper runtime configuration paramerts.
 * 
 * @author Walter Hoehn (wassa&#064;columbia.edu)
 */
public class ShibbolethConfigurationException extends Exception {

    /**
     * 'Required' Serial ID.
     */
    private static final long serialVersionUID = 3052563354463892233L;

    /**
     * Build an object embedding a String message.  Normally called for detected errors.
     * 
     * @param message - Text (in US English) describing the reason for raising the exception.
     */
    public ShibbolethConfigurationException(String message) {
        super(message);
        }

    /**
     * Build an object which embeds an message an exception. 
     * Normally called to pass on errors found at a lower level.
     * 
     * @param message - Text (in US English) describing the reasdon for raising the exception.
     * @param t - Cause for the failure as returned by the lower level component. 
     */
    public ShibbolethConfigurationException(String message, Throwable t) {
        super(message,t);
    }
}
