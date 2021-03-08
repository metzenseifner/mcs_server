package at.ac.uibk.mcsconnect.common.api;

import java.util.function.Function;

/**
 *
 * @param <A> The thing being cached.
 * @param <V> A function to determine whether cached value is still good.
 */
public interface PreparableFactory<A, V> {

    Preparable<A, V> create(Preparable<A,V> a);

    Preparable<A, V> create(Preparable<A,V> a, Function<V, Boolean> validator);

}
