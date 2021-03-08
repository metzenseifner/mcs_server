package at.ac.uibk.mcsconnect.common.api;

import java.util.function.Function;

/**
 * This is used to wrap lambda functions that
 * throw checked exceptions. It converts them
 * to unchecked exceptions. This use case will exist
 * until the Java architects implement a better way
 * to handle checked exceptions within lambdas.
 *
 * @param <T> input type
 * @param <R> return type
 * @param <E> exception type
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {

    R apply(T t) throws E;

    /**
     *
     * @param function that throws a checked exception
     * @param <T> input type
     * @param <R> return type
     * @param <E> exception type
     * @return a functional interface of type Function<T, R> that throws a converted unchecked exception
     */
    static <T, R, E extends Throwable> Function<T, R> unchecked(ThrowingFunction<T, R, E> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}