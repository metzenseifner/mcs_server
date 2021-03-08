package at.ac.uibk.mcsconnect.common.api;

import at.ac.uibk.mcsconnect.functional.common.Result;

/**
 * Represents a generic functional interface for
 * the cache.
 *
 * @param <A>
 * @param <V>
 */
public interface Preparable<A, V> {
    Result<V> prepare(A arg);
}
