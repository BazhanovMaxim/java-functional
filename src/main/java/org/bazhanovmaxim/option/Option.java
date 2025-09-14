package org.bazhanovmaxim.option;

import org.bazhanovmaxim.function.ThrowingFunction;
import org.bazhanovmaxim.result.Try;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A tiny, immutable container representing "presence/absence" of a value.
 * Intentionally NOT an error carrier; use {@link Try} for failures.
 *
 * <p><b>Quick start</b>
 * <pre>{@code
 * Option.of("  hi  ")
 *       .map(String::trim)
 *       .filter(s -> !s.isEmpty())
 *       .apply(System.out::println); // -> "hi"
 *
 * String value = Option.<String>empty().ifPresentOrElse(
 *     () -> "present", () -> "fallback"
 * ); // -> "fallback"
 * }</pre>
 */
public final class Option<T> {

    private static final Option<?> EMPTY = new Option<>(null);

    private final T value;

    private Option(T value) {
        this.value = value;
    }

    /**
     * Wrap a value (may be null).
     *
     * <pre>{@code
     * Option<String> o = Option.of("A");
     * Option<String> n = Option.of(null); // empty semantics
     * }</pre>
     */
    public static <T> Option<T> of(T value) {
        return new Option<>(value);
    }

    /**
     * Alias of {@link #of(Object)} to mirror Optional API.
     *
     * <pre>{@code
     * Option<Integer> o = Option.ofNullable(42);
     * }</pre>
     */
    public static <T> Option<T> ofNullable(T value) {
        return new Option<>(value);
    }

    /**
     * Canonical empty Option singleton.
     *
     * <pre>{@code
     * Option<String> empty = Option.empty();
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public static <T> Option<T> empty() {
        return (Option<T>) EMPTY;
    }

    /**
     * Return the raw wrapped value (may be null).
     *
     * <pre>{@code
     * String s = Option.of("X").get(); // "X"
     * String n = Option.<String>empty().get(); // null
     * }</pre>
     */
    public T get() {
        return value;
    }

    /**
     * @return true if value != null.
     *
     * <pre>{@code
     * Option.of("x").isPresent(); // true
     * Option.empty().isPresent(); // false
     * }</pre>
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * @return true if value == null.
     *
     * <pre>{@code
     * Option.empty().isEmpty(); // true
     * }</pre>
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * @return true if value != null (readability sugar).
     */
    public boolean isNotEmpty() {
        return value != null;
    }

    /**
     * Execute a side-effect on the value if present, then return this.
     *
     * <pre>{@code
     * Option.of(10).apply(x -> log.info("x={}", x)); // keeps Option(10)
     * }</pre>
     */
    public Option<T> apply(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (value != null) action.accept(value);
        return this;
    }

    /**
     * Execute a side-effect regardless of presence, then return this.
     *
     * <pre>{@code
     * Option.of("A").and(() -> metrics.increment("visited"));
     * }</pre>
     */
    public Option<T> and(Runnable runnable) {
        Objects.requireNonNull(runnable).run();
        return this;
    }

    /**
     * If present, run the action.
     *
     * <pre>{@code
     * Option.of("ping").ifPresent(System.out::println); // prints
     * Option.<String>empty().ifPresent(System.out::println); // no-op
     * }</pre>
     */
    public void ifPresent(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (value != null) action.accept(value);
    }

    /**
     * If empty, run the action.
     *
     * <pre>{@code
     * Option.<String>empty().ifEmpty(() -> System.out.println("missing")); // prints
     * }</pre>
     */
    public void ifEmpty(Runnable action) {
        Objects.requireNonNull(action);
        if (value == null) action.run();
    }

    /**
     * If empty -> run {@code ifEmpty}; else -> run {@code orElse} with value.
     *
     * <pre>{@code
     * Option.of("A").ifEmptyOrElse(
     *     () -> log.warn("no value"),
     *     v -> log.info("got {}", v)
     * ); // logs "got A"
     * }</pre>
     */
    public void ifEmptyOrElse(Runnable ifEmpty, Consumer<? super T> orElse) {
        Objects.requireNonNull(ifEmpty);
        Objects.requireNonNull(orElse);
        if (value == null) ifEmpty.run();
        else orElse.accept(value);
    }

    /**
     * Map present value to another value; empty stays empty.
     *
     * <pre>{@code
     * Option<Integer> len = Option.of("abc").map(String::length); // Option(3)
     * }</pre>
     */
    public <U> Option<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return value != null ? ofNullable(mapper.apply(value)) : empty();
    }

    /**
     * Map to a raw value (mapper is applied even for null).
     * Useful when intentionally propagating nulls.
     *
     * <pre>{@code
     * Integer n = Option.<String>empty().mapTo(s -> s == null ? 0 : s.length()); // 0
     * }</pre>
     */
    public <U> U mapTo(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return mapper.apply(value);
    }

    /**
     * Flat-map present value into another Option; empty stays empty.
     *
     * <pre>{@code
     * Option<Integer> n = Option.of("42").flatMap(s -> {
     *     try { return Option.of(Integer.parseInt(s)); }
     *     catch (NumberFormatException e) { return Option.empty(); }
     * }); // Option(42)
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public <U> Option<U> flatMap(Function<? super T, ? extends Option<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        if (value == null) return empty();
        Option<? extends U> res = mapper.apply(value);
        return (Option<U>) Objects.requireNonNull(res);
    }

    /**
     * Keep value only if predicate holds; empty remains empty.
     *
     * <pre>{@code
     * Option<Integer> even = Option.of(6).filter(x -> x % 2 == 0); // present
     * Option<Integer> oddGone = Option.of(5).filter(x -> x % 2 == 0); // empty
     * }</pre>
     */
    public Option<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (value == null) return this;
        return predicate.test(value) ? this : empty();
    }

    /**
     * Throw lazily if empty.
     *
     * <pre>{@code
     * String v = Option.<String>empty().orElseThrow(() -> new IllegalStateException("nope"));
     * // throws IllegalStateException
     * }</pre>
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        return Optional.ofNullable(value).orElseThrow(exceptionSupplier);
    }

    /**
     * Java Optional adapter.
     *
     * <pre>{@code
     * Optional<String> opt = Option.of("x").toOptional();
     * }</pre>
     */
    @Contract(pure = true)
    public @NotNull Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }

    /**
     * @return true if wrapped value is instance of {@code cls}.
     *
     * <pre>{@code
     * Option.of("x").isInstance(CharSequence.class); // true
     * }</pre>
     */
    public boolean isInstance(Class<?> cls) {
        Objects.requireNonNull(cls);
        return value != null && cls.isInstance(value);
    }

    /**
     * If value is instance of {@code U}, cast and return; otherwise empty.
     *
     * <pre>{@code
     * Option<Number> n = Option.of(10);
     * Option<Integer> i = n.ifInstance(Integer.class); // present(10)
     * }</pre>
     */
    public <U> Option<U> ifInstance(Class<U> cls) {
        Objects.requireNonNull(cls);
        if (value != null && cls.isInstance(value)) return Option.ofNullable(cls.cast(value));
        return Option.empty();
    }

    /**
     * If value is NOT instance of {@code U}, keep it; otherwise empty.
     *
     * <pre>{@code
     * Option<String> s = Option.of("x").ifNotInstance(Integer.class); // present
     * }</pre>
     */
    public <U> Option<T> ifNotInstance(Class<U> cls) {
        Objects.requireNonNull(cls);
        if (value == null) return empty();
        return cls.isInstance(value) ? empty() : this;
    }

    /**
     * If instance, run action.
     *
     * <pre>{@code
     * Option.of("abc").ifInstance(CharSequence.class, cs -> log.info("len={}", cs.length()));
     * }</pre>
     */
    public void ifInstance(Class<?> cls, Consumer<? super T> action) {
        Objects.requireNonNull(cls);
        Objects.requireNonNull(action);
        if (value != null && cls.isInstance(value)) action.accept(value);
    }

    /**
     * If instance, run runnable.
     *
     * <pre>{@code
     * Option.of("abc").ifInstance(String.class, () -> metrics.increment("string_seen"));
     * }</pre>
     */
    public void ifInstance(Class<?> cls, Runnable runnable) {
        Objects.requireNonNull(cls);
        Objects.requireNonNull(runnable);
        if (value != null && cls.isInstance(value)) runnable.run();
    }

    /**
     * Transform present value via mapper; otherwise compute fallback.
     *
     * <pre>{@code
     * int size = Option.of("abc").ifPresentOrElse(String::length, () -> 0); // 3
     * }</pre>
     */
    public <R> R ifPresentOrElse(Function<? super T, ? extends R> mapper,
                                 Supplier<? extends R> orElseGet) {
        Objects.requireNonNull(mapper);
        Objects.requireNonNull(orElseGet);
        return value != null ? mapper.apply(value) : orElseGet.get();
    }

    /**
     * Branch between two suppliers, ignoring the actual value.
     *
     * <pre>{@code
     * String tag = Option.<String>empty().ifPresentOrElse(() -> "present", () -> "empty"); // "empty"
     * }</pre>
     */
    public <R> R ifPresentOrElse(Supplier<? extends R> ifPresent,
                                 Supplier<? extends R> orElseGet) {
        Objects.requireNonNull(ifPresent);
        Objects.requireNonNull(orElseGet);
        return value != null ? ifPresent.get() : orElseGet.get();
    }

    // ---------- Bridging to Try (safe error handling) ----------

    /**
     * Run a throwing function against the contained value (if present) and
     * capture the result as {@link Try}. If empty, returns Failure(NPE).
     *
     * <pre>{@code
     * Try<Integer> t = Option.of("42").runCatching(Integer::parseInt); // Success(42)
     * Try<Integer> f = Option.<String>empty().runCatching(Integer::parseInt); // Failure(NPE)
     * }</pre>
     */
    public <U> Try<U> runCatching(ThrowingFunction<? super T, ? extends U> fn) {
        Objects.requireNonNull(fn);
        if (value == null) return Try.failure(new NullPointerException("Option is empty"));
        try {
            return Try.success(fn.apply(value));
        } catch (Exception e) {
            return Try.failure(e);
        }
    }

    /**
     * Keep this Option only if predicate holds; otherwise return empty.
     *
     * <pre>{@code
     * Option<String> s = Option.of("abc").takeIf(v -> v.length() > 2); // present("abc")
     * Option<String> n = Option.of("a").takeIf(v -> v.length() > 2);   // empty
     * }</pre>
     */
    public Option<T> takeIf(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (value == null) return empty();
        return predicate.test(value) ? this : empty();
    }

    /**
     * Keep this Option only if predicate is false; otherwise return empty.
     *
     * <pre>{@code
     * Option<String> s = Option.of("a").takeUnless(v -> v.length() > 2); // present("a")
     * Option<String> n = Option.of("abc").takeUnless(v -> v.length() > 2); // empty
     * }</pre>
     */
    public Option<T> takeUnless(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (value == null) return empty();
        return predicate.test(value) ? empty() : this;
    }

    /**
     * Return the value if present, else {@code other}.
     *
     * <pre>{@code
     * String v = Option.<String>empty().orElse("fallback"); // "fallback"
     * }</pre>
     */
    public T orElse(T other) {
        return value != null ? value : other;
    }

    /**
     * Return the value if present, else compute via {@code supplier}.
     *
     * <pre>{@code
     * String v = Option.<String>empty().orElseGet(() -> "fallback");
     * }</pre>
     */
    public T orElseGet(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);
        return value != null ? value : supplier.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Option<?> other)) return false;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return isPresent() ? "Option[" + value + "]" : "Option.empty";
    }
}
