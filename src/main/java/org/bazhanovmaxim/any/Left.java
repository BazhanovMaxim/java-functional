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
 * Left side of Either (error branch).
 *
 * <pre>{@code
 * Any<String,Integer> e = new Left<>("oops");
 * Any<String,Integer> r = e.map(x -> x * 2); // still Left("oops")
 * }</pre>
 */
public final class Left<L, R> implements Any<L, R> {

    private final L value;

    public Left(L value) {
        this.value = value;
    }

    private L getValue() {
        Objects.requireNonNull(value);
        return value;
    }

    @Override
    public boolean isLeft() {
        return true;
    }

    @Override
    public void ifLeft(@NotNull Consumer<? super L> action) {
        Objects.requireNonNull(action).accept(getValue());
    }

    @Override
    public Any<L, R> joinLeft(@NotNull Any<L, R> other) {
        return other;
    }

    @Override
    public Any<L, R> joinRight(@NotNull Any<L, R> other) {
        return this;
    }

    @Override
    public @Nullable L getLeft() {
        return value;
    }

    @Override
    public Any<L, R> filterOrElse(@NotNull Predicate<R> predicate, L orElse) {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Any<L, T> map(@NotNull Function<R, T> mapper) {
        return (Any<L, T>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Any<L, T> flatMap(@NotNull Function<R, Any<L, T>> mapper) {
        return (Any<L, T>) this;
    }

    @Override
    public <Result> Result fold(@NotNull Function<L, Result> leftMapper, @NotNull Function<R, Result> rightMapper) {
        Objects.requireNonNull(leftMapper);
        return leftMapper.apply(getValue());
    }

    @Override
    public void forEach(@NotNull Consumer<L> leftC, @NotNull Consumer<R> rightC) {
        Objects.requireNonNull(leftC).accept(getValue());
    }

    @Override
    public <L2> Any<L2, R> mapLeft(@NotNull Function<L, L2> leftMapper) {
        Objects.requireNonNull(leftMapper);
        return new Left<>(leftMapper.apply(getValue()));
    }

    @Override
    public <L2, R2> Any<L2, R2> bimap(@NotNull Function<L, L2> leftMapper,
                                      @NotNull Function<R, R2> rightMapper) {
        Objects.requireNonNull(leftMapper);
        Objects.requireNonNull(rightMapper);
        return new Left<>(leftMapper.apply(getValue()));
    }

    @Override
    public Any<R, L> swap() {
        return new Right<>(getValue());
    }

    @Override
    public @NotNull Option<R> toOption() {
        return Option.empty();
    }

    @Override
    public @NotNull Optional<R> toOptional() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "Left(" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Left)) return false;
        Left<?, ?> l = (Left<?, ?>) o;
        return Objects.equals(this.value, l.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
