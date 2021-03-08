package at.ac.uibk.mcsconnect.recorderservice.api;

import at.ac.uibk.mcsconnect.common.api.Immutable;
import at.ac.uibk.mcsconnect.functional.common.Effect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Map between {@link DublinCore} fields and
 * values.
 */
@JsonDeserialize(builder = Metadata.Builder.class)
@Immutable
public class Metadata {

    /**
     * A map of all metadata attributes.
     */
    //@JsonDeserialize
    private final Map<DublinCore, String> metadata;

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static class Builder {

        static Map<DublinCore, String> map = new HashMap<>();

        public Metadata build() {
            return new Metadata(map);
        }

        public Metadata.Builder with(Effect<Builder> builderEffect) {
            builderEffect.apply(this);
            return this;
        }

        public Metadata.Builder withProperty(DublinCore dcProperty, String value) {
            map.put(dcProperty, value);
            return this;
        }
    }

    private Metadata(Map<DublinCore, String> metadata) {
        this.metadata = Collections.unmodifiableMap(metadata);
    }


    public Map<DublinCore, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     *
     * @param dublinCore
     * @return value stored for a given {@link DublinCore} key.
     */
    public String get(final DublinCore dublinCore) {
        String str = metadata.get(dublinCore);
        if (str == null) {
            return "";
        } else
            return str;
    }

    /**
     * @return A as of the {@link DublinCore} keys contained in the metadata as strings.
     */
    public Set<String> names() {
        return metadata.keySet().stream()
                .map(DublinCore::toString)
                .collect(Collectors.toSet());
    }

    /**
     *
     * @return A set of {@link DublinCore} keys contained in the metadata.
     */
    public Set<DublinCore> keySet() {
        return metadata.keySet();
    }

    private static final String FORMATTER = "%s: \"%s\"";
    public String toString() {
        return metadata.entrySet().stream()
                .map( e -> String.format(FORMATTER, e.getKey().toString(), e.getValue()) )
                .collect(Collectors.joining("\n"));
    }
}
