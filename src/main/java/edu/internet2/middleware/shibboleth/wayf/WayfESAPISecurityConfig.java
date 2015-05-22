package edu.internet2.middleware.shibboleth.wayf;

import org.opensaml.ESAPISecurityConfig;

public class WayfESAPISecurityConfig extends ESAPISecurityConfig {
    /** Disable intrusion detection for ESAPI */
    public boolean getDisableIntrusionDetection() {
        return true;
    }

    /** Use default ESAPI intrusion detector implementation  */
    public String getIntrusionDetectionImplementation() {
        return "org.owasp.esapi.reference.DefaultIntrusionDetector";
    }

    /** Use UTF-8 as the default encoding */
    public String getCharacterEncoding() {
        return "UTF-8";
    }


}
