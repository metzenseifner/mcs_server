package at.ac.uibk.mcsconnect.common.api;

import at.ac.uibk.mcsconnect.functional.common.List;
import at.ac.uibk.mcsconnect.functional.osgi.OsgiPropertyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * OSGI Properties are read from Java property files with the extension .cfg
 * by the OSGI ConfigAdmin implementation.
 *
 * Decided to implement without interface to allow for static definitions.
 *
 */
public class OsgiProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsgiProperties.class);

    private final List<OsgiProperty> declaredProperties;

    private OsgiProperties(OsgiProperty... declarations) {
        this.declaredProperties = List.list(declarations);
    }

    public static OsgiProperties create(OsgiProperty... defs) {
        return new OsgiProperties(defs);
    }

    /**
     *  1. Create properties parser/reader
     *  2. apply reader to each element in {@link this#declaredProperties} to get key-value string pair to pass back to caller
     */
    public List<OsgiProperty> resolve(Map<String, ?> properties) {
        return resolve(properties, null);
    }

    public List<OsgiProperty> resolve(Map<String, ?> properties, LogLevel logLevel) {
        OsgiPropertyReader reader = OsgiPropertyReader.create(properties);
        List<OsgiProperty> props = this.declaredProperties.map(p -> p.resolve(reader));
        if (logLevel != null)  {
            switch (logLevel) {
                case INFO:
                    OsgiUtilities.logInfo(props.toJavaList());
                case DEBUG:
                    OsgiUtilities.logDebug(props.toJavaList());
            }
        }
        return props;
    }

    public enum LogLevel {
        INFO,
        DEBUG
    }
}
