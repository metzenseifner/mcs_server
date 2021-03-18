package at.ac.uibk.mcsconnect.common.api;

import at.ac.uibk.mcsconnect.functional.common.Function;
import at.ac.uibk.mcsconnect.functional.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * This class is intended to provide an efficient and thread-safe
 * cache for objects. Memoizer pattern.
 * <p>
 * DO NOT TOUCH THIS CODE UNLESS YOU KNOW EXACTLY WHAT YOU ARE DOING
 *
 * <p>
 * It maps some object to a Future-wrapped object. The reason
 * for the wrapper is to provided instantaneous puts, yet
 * block when another thread tries to access a value before
 * it has been computed / prepared / several verbs could work here.
 * <p>
 * I reworked this cache to support a more functional approach to error
 * handling (despite the cache not being programmed functionally).
 * Given that networking with SMPs is unstable, and that error handling
 * over IO is complex, I decided to reduce the output of Preparable to
 * either a Success of Failure subclass of {@see Result}. This way,
 * acquiring valid session can be made more reliable by the caller code (client code).
 */
public class PreparableCacheImpl<A, V> implements Preparable<A, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparableCacheImpl.class);

    private final Map<A, Future<Result<V>>> cache = new ConcurrentHashMap<>();
    private final Preparable<A, V> c; // This computable becomes a factory to create SshChannelShellLockable objects if not in map.
    private final Function<V, Boolean> isGood;

    /**
     * Constructs default configuration.
     *
     * @param c
     */
    public PreparableCacheImpl(Preparable<A, V> c) {
        this.c = c;
        this.isGood = (V) -> true; // Default
    }

    /**
     * Constructs configuration with extra test for stale values.
     *
     * @param c
     * @param isGood
     */
    public PreparableCacheImpl(Preparable<A, V> c, Function<V, Boolean> isGood) {
        this.c = c;
        this.isGood = isGood;
    }

    // Creates Preparable object the arg is preparable (it is a network target, whose session can be prepared)
    public Result<V> prepare(final A arg) {
        while (true) {
            LOGGER.debug("{} checking for {}", this, arg);
            Future<Result<V>> f = cache.get(arg);
            if (f == null) { // Tests whether arg in cache. A future must implement a Callable.
                f = addToCache(arg);
            }
            try {
                // At this point, f is certainly in the cache. Good point to test its validity against a conditional.
                Result<V> untestedResult = f.get(); // extract subclass Success or Failure
                Result<V> testedResult = untestedResult.filter(isGood); // convert Success to Failure if condition not met
                if (testedResult.isFailure()) {
                    LOGGER.debug(String.format("%s.prepare(%s) detected failure. Attempting to recover.", this, arg));
                    cache.remove(arg, f); // remove Failure
                    f = addToCache(arg); // add fresh one, which could  a Failure
                }
                return f.get(); // only returns success()
                // Upon any error (be sure to catch them all), remove from cache and return Result.Failure
            } catch (RuntimeException | ExecutionException | InterruptedException c) {
                LOGGER.debug(String.format("%s.prepare() failed: %s", this, c.getMessage()));
                cache.remove(arg, f);
                return Result.failure(c);
            }
        }
    }

    /**
     * Never call me directly, client caller should use cache.prepare().
     *
     * @param arg
     * @return
     */
    private Future addToCache(A arg) {
        Callable<Result<V>> callable = new Callable<Result<V>>() {
            public Result<V> call() {
                return c.prepare(arg); // return Success subtype to Future
            }
        };
        FutureTask<Result<V>> ft = new FutureTask<Result<V>>(callable);
        Future<Result<V>> f = cache.putIfAbsent(arg, ft);
        if (f == null) {
            LOGGER.debug("Adding new {} to {}", arg, this);
            f = ft; // keep ref to Future for get() later
            ft.run();
        }
        return f;
    }

    public String toString() {
        return "PreparableCache";
    }
}
