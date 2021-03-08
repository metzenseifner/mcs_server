package mcs.convention.plugin.versionmanager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VersionManager {

    private static String JACKSON_VERSION = "2.9.6";

    private static final Map<String, String> VERSIONS;
    private static Map<String, String> FAMILY;
    static {
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("jackson", "2.9.6"); // 2.11.4
        FAMILY = Collections.unmodifiableMap(tempMap);
    }

    static {
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("org.apache.cxf", "3.2.0"); // java 9 > 3.3.x release latest 3.3.9 // latest 3.2.14 // last major 2.7.13
        tempMap.put("osgi", "6.0.0");
        tempMap.put("junit", "5.6.0");
        tempMap.put("org.osgi.service.component.annotations", "1.4.0");
        tempMap.put("com.fasterxml.jackson", FAMILY.get("jackson"));
        tempMap.put("com.fasterxml.jackson.datatype", FAMILY.get("jackson"));
        tempMap.put("com.fasterxml.jackson.jaxrs", FAMILY.get("jackson"));
        tempMap.put("com.fasterxml.jackson.core", FAMILY.get("jackson"));
        tempMap.put("com.fasterxml.jackson.dataformat", FAMILY.get("jackson"));
        VERSIONS = Collections.unmodifiableMap(tempMap);
    }

    public String get(String groupId) {
        return VERSIONS.get(groupId);
    }
}
