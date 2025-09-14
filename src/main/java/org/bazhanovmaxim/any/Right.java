package org.bazhanovmaxim.any;

import org.bazhanovmaxim.option.Option;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Right side of Either (success branch).
 *
 * <pre>{@code
 * Any<String,Integer> r = new Right<>(2);
 * r = r.map(x -> x * 10); // Right(20)
 * }</pre>
 */
public final class Right<L, R> implements Any<L, R> {

    private final R value;

    public Right(R value) {
        this.value = value;
    }

    private R getValue() {
        Objects.requireNonNull(value);
        return value;
    }

    @Override
    public boolean isRight() {
        return true;
    }

    @Override
    public void ifRight(@NotNull Consumer<? super R> action) {
        Objects.requireNonNull(action).accept(getValue());
    }

    @Override
    public Any<L, R> joinLeft(@NotNull Any<L, R> other) {
        return this;
    }

    @Override
    public Any<L, R> joinRight(@NotNull Any<L, R> other) {
        return other;
    }

    @Override
    public @Nullable R getRight() {
        return value;
    }

    @Override
    public Any<L, R> filterOrElse(@NotNull Predicate<R> predicate, L orElse) {
        Objects.requireNonNull(predicate);
        return predicate.test(getValue()) ? this : new Left<>(orElse);
    }

    @Override
    public boolean exists(@NotNull Predicate<R> predicate) {
        Objects.requireNonNull(predicate);
        return predicate.test(getValue());
    }

    @Override
    public <T> Any<L, T> map(@NotNull Function<R, T> mapper) {
        Objects.requireNonNull(mapper);
        return new Right<>(mapper.apply(getValue()));
    }

    @Override
    public <T> Any<L, T> flatMap(@NotNull Function<R, Any<L, T>> mapper) {
        Objects.requireNonNull(mapper);
        return Objects.requireNonNull(mapper.apply(getValue()));
    }

    @Override
    public <Result> Result fold(@NotNull Function<L, Result> leftMapper, @NotNull Function<R, Result> rightMapper) {
        Objects.requireNonNull(rightMapper);
        return rightMapper.apply(getValue());
    }

    @Override
    public void forEach(@NotNull Consumer<L> leftC, @NotNull Consumer<R> rightC) {
        Objects.requireNonNull(rightC).accept(getValue());
    }

    @Override
    public <L2> Any<L2, R> mapLeft(@NotNull Function<L, L2> leftMapper) {
        Objects.requireNonNull(leftMapper);
        return new Right<>(getValue()); // left unchanged
    }

    @Override
    public <L2, R2> Any<L2, R2> bimap(@NotNull Function<L, L2> leftMapper,
                                      @NotNull Function<R, R2> rightMapper) {
        Objects.requireNonNull(leftMapper);
        Objects.requireNonNull(rightMapper);
        return new Right<>(rightMapper.apply(getValue()));
    }

    @Override
    public Any<R, L> swap() {
        return new Left<>(getValue());
    }

    @Override
    public @NotNull Option<R> toOption() {
        return Option.ofNullable(value);
    }

    @Override
    public @NotNull Optional<R> toOptional() {
        return Optional.ofNullable(value);
    }

    @Override
    public String toString() {
        return "Right(" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Right)) return false;
        Right<?, ?> r = (Right<?, ?>) o;
        return Objects.equals(this.value, r.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
