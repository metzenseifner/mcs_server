package at.ac.uibk.mcsconnect.functional.osgi;

import at.ac.uibk.mcsconnect.functional.common.Result;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.zone.ZoneRulesException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class OsgiPropertyReader {
    /**
     * Handles OSGI Configurations {@link Dictionary} by
     * <p>
     * It provides failure messages for the following cases
     * - null dictionary
     * - missing key
     * - invalid value (parsing error)
     */

    private Result<Map<String, String>> properties;


    public static OsgiPropertyReader create(Dictionary oldStyleOsgiProperties) {
        List<String> keys = Collections.list(oldStyleOsgiProperties.keys());
        Map<String, String> properties = keys.stream().collect(Collectors.toMap(Function.identity(), i -> oldStyleOsgiProperties.get(i).toString()));
        return new OsgiPropertyReader(readOsgiPropertiesDictionary(properties));
    }
    public static OsgiPropertyReader create(Map<String, ?> properties) {
        Map<String, String> sanitizedProps = properties.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));


        return new OsgiPropertyReader(readOsgiPropertiesDictionary(sanitizedProps));
    }

    private OsgiPropertyReader(Result<Map<String, String>> properties) {
        this.properties = properties;
    }

    private static Result<Map<String, String>> readOsgiPropertiesDictionary(Map<String, String> properties) {
        return Result.of(properties, "OSGI properties were null.");
    }


    /**
     * Helper
     *
     * @param properties
     * @param key
     * @return
     */
    private Result<String> getProperty(Map<String, String> properties, String key) {
        return Result.of(properties.get(key))
                .mapFailure(String.format("Property \"%s\" not found.", key));
    }

    public Result<String> getAsString(String key) {
        return properties.flatMap(props -> getProperty(props, key));
    }

    public Result<URI> getAsURI(String key) {
        Result<String> rURI = properties.flatMap(props -> getProperty(props, key));
        return rURI.flatMap(x -> {
            try {
                return Result.success(URI.create(x));
            } catch (Exception e) {
                return Result.failure(String.format("Invalid value while parsing property key \"%s\": \"%s\". Because %s", key, x, e), e);
            }
        });
    }

    public Result<LocalTime> getAsLocalTime(String key) {
        Result<String> rString = properties.flatMap(props -> getProperty(props, key));
        return rString.flatMap(x -> {
            try {
                return Result.success(LocalTime.parse(x));
            } catch (DateTimeParseException e) {
                return Result.failure(String.format("Invalid value while parsing property key \"%s\": \"%s\". Because %s", key, x, e), e);
            }
        });
    }

    public Result<Long> getAsLong(String key) {
        Result<String> rString = properties.flatMap(props -> getProperty(props, key));
        return rString.flatMap(x -> {
            try {
                return Result.success(Long.valueOf(x));
            } catch (NumberFormatException e) {
                return Result.failure(String.format("Invalid value while while parsing property key \"%s\": \"%s\". Because %s", key, x, e), e);
            }
        });
    }

    public Result<Integer> getAsInt(String key) {
        Result<String> rString = properties.flatMap(props -> getProperty(props, key));
        return rString.flatMap(x -> {
            try {
                return Result.success(Integer.valueOf(x));
            } catch (NumberFormatException e) {
                return Result.failure(String.format("Invalid value while while parsing property key \"%s\": \"%s\". Because %s", key, x, e), e);
            }
        });
    }

    public Result<TimeUnit> getAsTimeUnit(String key) {
        Result<String> rString = properties.flatMap(props -> getProperty(props, key));
        return rString.flatMap(x -> {
            try {
                return Result.success(TimeUnit.valueOf(x));
            } catch (IllegalArgumentException e) {
                return Result.failure(String.format("Invalid value while while parsing property key \"%s\": \"%s\". Because %s", key, x, e), e);
            }
        });
    }

    public Result<ChronoUnit> getAsChronoUnit(String key) {
        Result<String> rString = properties.flatMap(props -> getProperty(props, key));
        return rString.flatMap(x -> {
            try {
                return Result.success(ChronoUnit.valueOf(x));
            } catch (IllegalArgumentException e) {
                return Result.failure(String.format("Invalid value while while parsing property key \"%s\": \"%s\". Because %s", key, x, e), e);
            }
        });
    }

    public Result<ZoneId> getAsZoneId(String key) {
        Result<String> rString = properties.flatMap(props -> getProperty(props, key));
        return rString.flatMap(x -> {
            try {
                return Result.success(ZoneId.of(x));
            } catch (ZoneRulesException e) { // TODO: figure out specifically which exceptions can be thrown
                return Result.failure(String.format("Invalid value while parsing property key \"%s\": \"%s\". Because %s", key, x, e), e);
            }
        });
    }

    public Result<Path> getAsPath(String key) {
        Result<String> rString = properties.flatMap(props -> getProperty(props, key));
        return rString.flatMap(x -> {
            try {
                return Result.success(FileSystems.getDefault().getPath(x));
            } catch (Exception e) {
                return Result.failure(String.format("Invalid value while parsing property key \"%s\": \"%s\". Because %s", key, x, e), e);
            }
        });
    }

    public Result<Pattern> getAsPattern(String key) {
        Result<String> rString = properties.flatMap(props -> getProperty(props, key));
        return rString.flatMap(x -> {
            try {
                return Result.success(Pattern.compile(x));
            } catch (Exception e) {
                return Result.failure(String.format("Invalid value while parsing property key \"%s\": \"%s\". Because %s", key, x, e), e);
            }
        });
    }

    public <T> Result<T> getAsType(final Map<String, String> dictionary, final String name, final Function<String, Result<T>> function) {
        Result<String> rString = getProperty(dictionary, name);
        return rString.flatMap(s -> {
            try {
                return function.apply(s);
            } catch (Exception e) {
                return Result.failure(String.format("Invalid value while parsing property key \"%s\": \"%s\". Because %s", name, s, e), e);
            }
        });
    }

    public boolean isEmpty() {
        return properties.isFailure() || properties.isEmpty()
                ? true
                : false;
    }


}
