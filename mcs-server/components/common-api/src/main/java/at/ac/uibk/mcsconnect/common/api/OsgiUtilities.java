package at.ac.uibk.mcsconnect.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class OsgiUtilities {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsgiUtilities.class);

    public static void logInfo(List<OsgiProperty> metadata) {
        metadata.stream()
                .forEach(e -> LOGGER.info(e.toString()));
    }

    public static void logDebug(List<OsgiProperty> metadata) {
        metadata.stream()
                .forEach(e -> LOGGER.debug(e.toString()));
    }
}
