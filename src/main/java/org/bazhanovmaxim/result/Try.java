package org.bazhanovmaxim.result;

import org.bazhanovmaxim.any.Any;
import org.bazhanovmaxim.any.Left;
import org.bazhanovmaxim.any.Right;
import org.bazhanovmaxim.option.Option;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A small, immutable Result-like type: either Success(value) or Failure(exception).
 * Mirrors the spirit of Kotlin's Result / FP Try monad.
 *
 * <p><b>Quick start</b>
 * <pre>{@code
 * Try<Integer> t = Try.success(10)
 *     .map(x -> x * 2)                // Success(20)
 *     .onSuccess(v -> log.info("v={}", v));
 *
 * int v = Try.failure(new IllegalStateException("boom"))
 *     .recover(ex -> 0);              // 0
 * }</pre>
 */
public sealed interface Try<T> permits Try.Success, Try.Failure {

    /**
     * Create Success.
     *
     * <pre>{@code
     * Try<String> ok = Try.success("ok");
     * }</pre>
     */
    static <T> Try<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Create Failure.
     *
     * <pre>{@code
     * Try<String> fail = Try.failure(new IOException("io"));
     * }</pre>
     */
    static <T> Try<T> failure(Exception e) {
        return new Failure<>(Objects.requireNonNull(e));
    }

    /**
     * @return true if this is Success.
     *
     * <pre>{@code
     * Try.success(1).isSuccess(); // true
     * }</pre>
     */
    boolean isSuccess();

    /**
     * @return true if this is Failure.
     *
     * <pre>{@code
     * Try.failure(new RuntimeException()).isFailure(); // true
     * }</pre>
     */
    default boolean isFailure() {
        return !isSuccess();
    }

    // ---------- Constructors ----------

    /**
     * @return value or null (on Failure).
     *
     * <pre>{@code
     * Integer v = Try.success(5).getOrNull(); // 5
     * }</pre>
     */
    T getOrNull();

    /**
     * @return exception or null (on Success).
     *
     * <pre>{@code
     * Exception e = Try.failure(new Exception("x")).exceptionOrNull(); // not null
     * }</pre>
     */
    Exception exceptionOrNull();

    // ---------- Transformations ----------

    /**
     * Map success value. Failure propagates unchanged.
     *
     * <pre>{@code
     * Try<Integer> t = Try.success(3).map(x -> x + 1); // Success(4)
     * }</pre>
     */
    default <U> Try<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isSuccess()) return success(mapper.apply(getOrNull()));
        return failure(exceptionOrNull());
    }

    /**
     * Flat-map success value. Failure propagates unchanged.
     *
     * <pre>{@code
     * Try<Integer> t = Try.success("42").flatMap(s -> {
     *     try { return Try.success(Integer.parseInt(s)); }
     *     catch (NumberFormatException e) { return Try.failure(e); }
     * });
     * }</pre>
     */
    default <U> Try<U> flatMap(Function<? super T, Try<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (isSuccess()) return Objects.requireNonNull(mapper.apply(getOrNull()));
        return failure(exceptionOrNull());
    }

    // ---------- Side effects ----------

    /**
     * Run action on success; returns this.
     *
     * <pre>{@code
     * Try.success(10).onSuccess(v -> log.info("v={}", v));
     * }</pre>
     */
    default Try<T> onSuccess(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (isSuccess()) action.accept(getOrNull());
        return this;
    }

    /**
     * Run action on failure; returns this.
     *
     * <pre>{@code
     * Try.failure(new RuntimeException("x")).onFailure(ex -> metrics.increment("fail"));
     * }</pre>
     */
    default Try<T> onFailure(Consumer<? super Exception> action) {
        Objects.requireNonNull(action);
        if (isFailure()) action.accept(exceptionOrNull());
        return this;
    }

    // ---------- Recovery / Folding ----------

    /**
     * Get value or supplier result if failure.
     *
     * <pre>{@code
     * int x = Try.failure(new Exception()).getOrElse(() -> 0); // 0
     * }</pre>
     */
    default T getOrElse(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);
        return isSuccess() ? getOrNull() : supplier.get();
    }

    /**
     * Recover failure by mapping exception to a value.
     *
     * <pre>{@code
     * int x = Try.failure(new Exception("e")).recover(ex -> 1); // 1
     * }</pre>
     */
    default T recover(Function<? super Exception, ? extends T> recoverFn) {
        Objects.requireNonNull(recoverFn);
        return isSuccess() ? getOrNull() : recoverFn.apply(exceptionOrNull());
    }

    /**
     * Fold into a single value.
     *
     * <pre>{@code
     * String s = Try.success(2).fold(
     *     ex -> "fail: " + ex.getMessage(),
     *     v  -> "ok: " + v
     * ); // "ok: 2"
     * }</pre>
     */
    default <R> R fold(Function<? super Exception, ? extends R> onFailure,
                       Function<? super T, ? extends R> onSuccess) {
        Objects.requireNonNull(onFailure);
        Objects.requireNonNull(onSuccess);
        return isSuccess() ? onSuccess.apply(getOrNull()) : onFailure.apply(exceptionOrNull());
    }

    /**
     * Return value or throw (wrap checked exceptions into RuntimeException).
     *
     * <pre>{@code
     * int v = Try.success(7).getOrThrow(); // 7
     * }</pre>
     */
    default T getOrThrow() {
        if (isSuccess()) return getOrNull();
        var e = exceptionOrNull();
        if (e instanceof RuntimeException re) throw re;
        throw new RuntimeException(e);
    }

    /**
     * Convert this Try into an Either-like value:
     * Success -> Right(value), Failure -> Left(exception).
     *
     * <pre>{@code
     * Any<Exception,Integer> e = Try.success(5).toEither(); // Right(5)
     * }</pre>
     */
    default Any<Exception, T> toEither() {
        return isSuccess()
                ? new Right<>(getOrNull())
                : new Left<>(exceptionOrNull());
    }

    /**
     * Convert Success -> Option.of(value), Failure -> Option.empty().
     *
     * <pre>{@code
     * Option<Integer> o = Try.success(5).toOption(); // present
     * }</pre>
     */
    default Option<T> toOption() {
        return isSuccess() ? Option.of(getOrNull()) : Option.empty();
    }

    // ---------- Variants ----------

    /**
     * Success variant.
     */
    final class Success<T> implements Try<T> {
        private final T value;

        public Success(T value) {
            this.value = value;
        }

        public boolean isSuccess() {
            return true;
        }

        public T getOrNull() {
            return value;
        }

        public Exception exceptionOrNull() {
            return null;
        }

        @Override
        public String toString() {
            return "Success(" + value + ")";
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Success<?> s) && Objects.equals(value, s.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }
    }

    /**
     * Failure variant.
     */
    final class Failure<T> implements Try<T> {
        private final Exception e;

        public Failure(Exception e) {
            this.e = Objects.requireNonNull(e);
        }

        public boolean isSuccess() {
            return false;
        }

        public T getOrNull() {
            return null;
        }

        public Exception exceptionOrNull() {
            return e;
        }

        @Override
        public String toString() {
            return "Failure(" + e + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Failure<?> f)) return false;
            return e.getClass().equals(f.e.getClass()) && Objects.equals(e.getMessage(), f.e.getMessage());
        }

        @Override
        public int hashCode() {
            return Objects.hash(e.getClass(), e.getMessage());
        }
    }
}
