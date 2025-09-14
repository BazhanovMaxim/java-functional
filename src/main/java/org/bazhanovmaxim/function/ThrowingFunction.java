package org.bazhanovmaxim.function;

/**
 * A Function that may throw a checked exception.
 *
 * <pre>{@code
 * ThrowingFunction<String, Integer> parse = Integer::parseInt;
 * }</pre>
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;
}
