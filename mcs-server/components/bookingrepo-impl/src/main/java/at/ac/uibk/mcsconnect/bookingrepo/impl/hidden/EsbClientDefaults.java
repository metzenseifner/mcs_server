package at.ac.uibk.mcsconnect.bookingrepo.impl.hidden;

import java.net.URI;

public abstract class EsbClientDefaults {
    public static final String DEFAULT_ESB_USERNAME = "admin";
    public static final String DEFAULT_ESB_PASSWORD = "admin";
    public static final URI DEFAULT_ESB_URL = URI.create("https://esb01.uibk.ac.at/cxf/vle-connect/sis");
}
