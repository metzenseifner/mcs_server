package at.ac.uibk.mcsconnect.recorderservice.api;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the constants of Dublin Core.
 *
 * Note: I chose to implement this because the existing implementations
 * pollute implementation namespaces. Constants in modern Java belong
 * in an Enum, not an interface. This way they are strongly typed.
 */
public enum DublinCore {

    COVERAGE("coverage"),
    CREATOR("creator"),
    RELATION("relation"),
    SOURCE("source"),
    SUBJECT("subject"),
    TITLE("title");

    String propertyName;

    DublinCore(String propertyName) {
        this.propertyName = propertyName;
    };

    public static List<DublinCore> getConstants() {
        return Arrays.asList(DublinCore.values());
    }

    @Override
    public String toString() {
        return propertyName;
    }
}