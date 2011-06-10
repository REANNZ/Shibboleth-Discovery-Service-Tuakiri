package edu.internet2.middleware.shibboleth.wayf.plugins;

/**
 * 
 * This Exception can be signalled by a plugin to indicate to the WAYF that it has handled the 
 * request and all processing should stop.
 * 
 * @author Rod Widdowson
 */
public class WayfRequestHandled extends Exception {

   /**
    * Required Serialization constant.
    */
    private static final long serialVersionUID = 3022489208153734092L;

}
