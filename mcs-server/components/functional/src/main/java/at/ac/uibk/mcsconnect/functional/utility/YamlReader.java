package at.ac.uibk.mcsconnect.functional.utility;

import at.ac.uibk.mcsconnect.functional.common.Result;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The starting point will hvave to be a string
 * because as experiments have shown, arrays cannot be
 * cast into Map<String, String> where String is the empty String.
 *
 * ----- old exp
 *
 *
 * This read was designed to read a YAML file containing
 * a single YAML document i.e. one occurence of "---"
 *
 * SnakeYAML allows you to construct a Java object of any type.
 * https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-providing-the-top-level-type
 *
 * This yaml reader was written to process yaml in the format below.
 * This format handles long lists better than the Configuration Admin configuration
 * files which are property based.
 *
 * Functions
 * 1. Read a file and return the content as a YAML map
 *
 * Read yaml files formatted as objects
 * Object1
 *  - elem1
 *  Subobject1
 *    - subelem1
 * Object2
 *  - elem1
 *
 * YAML is a recursive structure.
 * This reader should simple read one layer.
 *
 *
 */
public class YamlReader {

    private final Result<YamlData> yaml; // this could be Result<String> but all types output by yaml.load support toString().
    private final String source; // track where the yaml came from

    private YamlReader(Result<YamlData> yaml, String source) {
        this.yaml = yaml; // propogates error to methods
        this.source = source;
    }

    public static YamlReader fileYamlReader(String path) {
        return new YamlReader(readYamlFromFile(path), String.format("File: %s", path));
    }
    public static <T> YamlReader fileYamlReader(Path path) {
        return new YamlReader(readYamlFromFile(path.toString()), String.format("File: %s", path));
    }

    public static YamlReader stringYamlReader(String yaml) {
        return new YamlReader(readYamlFromString(yaml), String.format("String: %s", yaml));
    }

    /** Recursive reader */
    public static YamlReader objectYamlReader(Object yaml) {
        return objectToYamlReader(yaml).successValue(); // TODO fix could fail! (unlikely if used recursively)
    }

    /** Utility parse object back into yaml string */
    private static Result<YamlReader> objectToYamlReader(Object input) {
        Result<Object> rInput = Result.of(input);
        return rInput.flatMap(in -> {
            try {
                Yaml yaml = new Yaml(new SafeConstructor());
                String backToYaml = yaml.dump(input); // could be map to java.util.LinkedHashMap, which cannot be easily converted into String
                YamlReader recursiveReader = YamlReader.stringYamlReader(backToYaml);
                return Result.success(recursiveReader);
            } catch (ClassCastException c) {
                return Result.failure(String.format("Could not parse YAML as map."));
            }
        });
    }

    /**
     * Tries to read yaml data from all basic yaml types.
     *
     * If yamlToMap fails but yamlToList is a list of maps, the result
     * will be a list of mappings.
     *
     * string (single token)
     * list of string
     * map
     *
     * @param input
     * @return
     */
    private static Result<YamlData> readYamlFromString(String input) {
        Result<YamlData> data = yamlToMap(input).orElse(() -> yamlToList(input)).orElse(() -> yamlToTokens(input));
        return data;
    }

    private static Result<YamlData> readYamlFromFile(String path) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(path)))
        {
            String result = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
            Result<YamlData> data = yamlToMap(result).orElse(() -> yamlToList(result)).orElse(() -> yamlToTokens(result));
            return data;
        } catch (NullPointerException n) {
            return Result.failure(n);
        } catch (IOException i) {
            return Result.failure(i);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    private static Result<Map<String, Object>> readYamlFile(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            Yaml yaml = new Yaml(new SafeConstructor()); // limit objects to standard Java objects
            Map<String, Object> yamlObjects = yaml.load(inputStream);
            return Result.of(yamlObjects);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    /** Utility parse string as map */
    private static Result<YamlData> yamlToMap(String input) {
        Result<String> rInput = Result.of(input);
        return rInput.flatMap(in -> {
            try {
                Yaml yaml = new Yaml(new SafeConstructor());
                Map<String, Object> yamlMap = yaml.load(input); // could be map to java.util.LinkedHashMap, which cannot be easily converted into String
                Map<String, YamlReader> recursiveReader = yamlMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> YamlReader.objectYamlReader(e.getValue())));
                return Result.success(YamlData.fromMap(recursiveReader));
            } catch (ClassCastException c) {
                return Result.failure(String.format("Could not parse YAML as map."));
            }
        });
    }

    /** Utility parse string as list */
    private static Result<YamlData> yamlToList(String input) {
        Result<String> rInput = Result.of(input);
        return rInput.flatMap(in -> {
            try {
                Yaml yaml = new Yaml(new SafeConstructor());
                List<String> yamlMap = yaml.load(input);
                return Result.success(YamlData.fromList(yamlMap));
            } catch (ClassCastException c) {
                return Result.failure(String.format("Could not parse YAML as map."));
            }
        });
    }

    /** Utility parse string as tokens */
    private static Result<YamlData> yamlToTokens(String input) {
        Result<String> rInput = Result.of(input);
        return rInput.flatMap(in -> {
            try {
                Yaml yaml = new Yaml(new SafeConstructor());
                String yamlMap = yaml.load(input);
                return Result.success(YamlData.fromString(yamlMap));
            } catch (ClassCastException c) {
                return Result.failure(String.format("Could not parse YAML as map."));
            }
        });
    }

    public Result<List<String>> getAsList() {
        return this.yaml.map(y -> y.getAsList());
    }
    public Result<Map<String, YamlReader>> getAsMap() {
        return this.yaml.map(y -> y.getAsMap());
    }
    public Result<String> getAsString() {
        return this.yaml.map(y -> y.getAsString());
    }

    public Result<YamlReader> getKey(String key, Map<String, YamlReader> map) {
        Result<String> rKey = Result.of(key);
        return rKey.flatMap(k -> {
            try {
                return Result.success(map.get(k));
            } catch (Exception e) {
                return Result.failure(String.format("Could not find key \"%s\" in map: %s", k, map));
            }
        });
    }



//    /**
//     * Provides a map-like interface.
//     *
//     * Assumes {@link this#yaml} contains a {@link Map<String, String>}.
//     *
//     * @param key
//     * @return
//     */
//    public Result<String> get(String key) {
//        try {
//            return yaml.flatMap(raw -> Result.of((Map<String, String>) raw)).flatMap(map -> get(map, key));
//        } catch (Exception e) {
//            return Result.failure(String.format("Invalid get key \"%s\" from yaml: %s", key, yaml.toString()));
//        }
//    }
//
//    /** Helper to handle case where {@link Map#get} returns null */
//    private Result<String> get(Map<String, String> map, String key) {
//        return Result.of(map.get(key));
//    }
//
//    public Result<List<String>> getAsStringList() {
//        return yaml.flatMap(x -> {
//            try {
//                return Result.success((ArrayList<String>) x);
//            } catch (Exception e) {
//                return Result.failure(e);
//            }
//        });
//    }
//
//    public Result<List<String>> getAsStringList(String name) {
//        return getAsList(name, Function.identity());
//    }
//
//    /** like getProperty in PropertyReader */
//    private Result<String> getYaml(String yaml, String key) {
//        return Result.of(yaml. (name)).mapFailure(String.format("Property \"%s\" no found in %s", name, this.source));
//    }
//
//    public <T> Result<List<T>> getAsList(String name, Function<String, T> f) {
//        Result<String> rString = yaml.flatMap(yaml -> get(yaml, name));
//        return rString.flatMap(s -> {
//            try {
//                return Result.success(List.fromSeparatedString(s, ',').map(f));
//            } catch (NumberFormatException e) {
//                return Result.failure(String.format("Invalid value while parsing property %s: %s", name, s));
//            }
//        });
//    }
//
//    public Result<List<String>> getAsList() {
//        return yaml.flatMap(x -> {
//            try {
//                return Result.success((ArrayList<String>) x);
//            } catch (Exception e) {
//                return Result.failure(e);
//            }
//        });
//    }
//
////    public <T> Result<List<T>> getAsListOf(Class<T> type) {
////        return data.flatMap(x -> {
////            try {
////                return Result.success((List<T>) x);
////            } catch (Exception e) {
////                return Result.failure(e);
////            }
////        });
////    }
//
//    //public Result<Set<String>> getAsSet(String key) {
//    //    Result<List<String>> rList = getAsList();
//    //    return rList.map(l -> new HashSet<>(l));
//    //}
//
//    public Result<Map<String, String>> getAsMap() {
//        return yaml.flatMap(x -> {
//            try {
//                return Result.success((Map<String, String>) x);
//            } catch (Exception e) {
//                return Result.failure(e);
//            }
//        });
//    }
//
//    /** Most powerful function
//     */
//    public <T> Result<T> getAsType(final Function<Object, Result<T>> function, final String key) {
//        return yaml.flatMap(s -> {
//            try {
//                return function.apply(s);
//            } catch (Exception e) {
//                return Result.failure(String.format("Invalid valud while parsing key \"%s\" in yaml: %s", key, s));
//            }
//        });
//    }
}
