package at.ac.uibk.mcsconnect.common.api;

import at.ac.uibk.mcsconnect.functional.common.Function;
import at.ac.uibk.mcsconnect.functional.osgi.OsgiPropertyReader;

import java.util.function.Consumer;

/**
 * Represents a key-value pair, but also encapsulates
 * logic to compute the value of a given key using a provided function,
 * and finally a callback for setting the value.
 *
 * Decided to implement without interface to allow for static definitions.
 *
 * @param <A> the type of the value
 */
public class OsgiProperty<A> {

    private final String key;
    private A value;
    private final Function<OsgiPropertyReader, A> valueResolver;
    private final Consumer<A> setter;
    private final boolean isValueLoggable;
    private final String LOG_FORMATTER;
    private final String LOG_DISABLED_FORMATTER;

    private OsgiProperty(String key, Function<String, Function<OsgiPropertyReader, A>> valueResolver, Consumer<A> setter, Boolean isValueLoggable) {
        this.key = key;
        this.valueResolver = valueResolver.apply(this.key);
        this.setter = setter;
        this.isValueLoggable = isValueLoggable;
        this.LOG_FORMATTER = "Setting key \"%s\" to value: \"%s\"";
        this.LOG_DISABLED_FORMATTER = "Setting key \"%s\". Logging of this value has been disabled.";
    }

    public static <A> OsgiProperty create(String key, Function<String, Function<OsgiPropertyReader, A>> valueResolver, Consumer<A> setter) {
        return new OsgiProperty<A>(key, valueResolver, setter, true);
    }

    public static <A> OsgiProperty create(String key, Function<String, Function<OsgiPropertyReader, A>> valueResolver, Consumer<A> setter, Boolean isValueLoggable) {
        return new OsgiProperty(key, valueResolver, setter, isValueLoggable);
    }

    /** The main resolve method */
    public OsgiProperty<A> resolve(OsgiPropertyReader reader) {
        A result = resolveHelper(reader);
        setter.accept(result);
        this.value = result;
        return this;
    };

    /** Testable resolver */
    private A resolveHelper(OsgiPropertyReader reader) {
        return this.valueResolver.apply(reader);
    }

    /** Possibly useful for logging */
    private A resolveIfNotAvailable(OsgiPropertyReader reader) {
        if (this.value == null) {
            A result = resolveHelper(reader);
            setter.accept(result);
            this.value = result;
        }
        return this.value;
    }

    public String getKey() {
        return key;
    }

    public Function<OsgiPropertyReader, A> getValueResolver() {
        return this.valueResolver;
    }

    public Boolean isValueLoggable() {
        return this.isValueLoggable;
    }

    @Override
    public String toString() {
        return this.isValueLoggable
                ? String.format(this.LOG_FORMATTER, getKey(), this.value)
                : String.format(this.LOG_DISABLED_FORMATTER, getKey());
    }

}
