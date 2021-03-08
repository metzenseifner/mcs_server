package at.ac.uibk.mcsconnect.functional.utility;

import java.util.List;
import java.util.Map;

/**
 * This class handles the three cases of yaml data.
 *
 * In all cases, get() should return a String.
 *
 */
public abstract class YamlData {
    abstract Map<String, YamlReader> getAsMap();
    abstract List<String> getAsList();
    abstract String getAsString();

    /** list token: "- token" */
    private static class YamlList extends YamlData {
        private final List<String> data;
        private YamlList(List<String> data) {
            super();
            this.data = data;
        }
        public Map<String, YamlReader> getAsMap() {
            throw new IllegalStateException("Method getAsMap() called on a YamlList instance");
        };
        public List<String> getAsList() {
            return this.data;
        };
        public String getAsString() {
            throw new IllegalStateException("Method getAsString() called on a YamlList instance");
        };
    }

    /** map token: "token:" **/
    private static class YamlMap extends YamlData {
        private final Map<String, YamlReader> data;
        private YamlMap(Map<String, YamlReader> data) {
            super();
            this.data = data;
        }
        public Map<String, YamlReader> getAsMap() {
            return this.data;
        };
        public List<String> getAsList() {
            throw new IllegalStateException("Method getAsList() called on a YamlMap instance");
        };
        public String getAsString() {
            throw new IllegalStateException("Method getAsString() called on a YamlMap instance");
        };
    }

    /** token: "token" */
    private static class YamlString extends YamlData {
        private final String data;
        private YamlString(String data) {
            super();
            this.data = data;
        }
        public Map<String, YamlReader> getAsMap() {
            throw new IllegalStateException("Method getAsMap() called on a YamlString instance");
        };
        public List<String> getAsList() {
            throw new IllegalStateException("Method getAsList() called on a YamlString instance");
        };
        public String getAsString() {
            return this.data;
        };
    }

    public static YamlData fromMap(Map<String, YamlReader> input) {
        return new YamlMap(input);
    }
    public static YamlData fromList(List<String> input) {
        return new YamlList(input);
    }
    public static YamlData fromString(String input) {
        return new YamlString(input);
    }
}
