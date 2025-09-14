package org.bazhanovmaxim.option;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionMoreBranchesTest {

    @Test
    void equals_coversAllBranches() {
        var a = Option.of("x");
        var b = Option.of("x");
        var c = Option.of("y");

        // self branch
        assertThat(a.equals(a)).isTrue();

        // same value -> true
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());

        // different value -> false
        assertThat(a).isNotEqualTo(c);

        // different type -> false
        assertThat(a.equals("x")).isFalse();

        // null -> false
        assertThat(a.equals(null)).isFalse();
    }

    @Test
    void isInstance_trueAndFalseBranches() {
        assertThat(Option.of("x").isInstance(CharSequence.class)).isTrue();
        assertThat(Option.of(42).isInstance(CharSequence.class)).isFalse();
    }

    @Test
    void ifInstance_consumer_runsOnlyOnMatch() {
        AtomicBoolean ran = new AtomicBoolean(false);
        Option.of("abc").ifInstance(CharSequence.class, v -> ran.set(true));
        assertThat(ran).isTrue();

        ran.set(false);
        Option.of(123).ifInstance(CharSequence.class, v -> ran.set(true));
        assertThat(ran).isFalse();
    }

    @Test
    void ifInstance_runnable_runsOnlyOnMatch() {
        AtomicBoolean ran = new AtomicBoolean(false);
        Option.of("abc").ifInstance(String.class, () -> ran.set(true));
        assertThat(ran).isTrue();

        ran.set(false);
        Option.of(123).ifInstance(String.class, () -> ran.set(true));
        assertThat(ran).isFalse(); // ветка "не совпал класс"
    }

    @Test
    void ifNotInstance_bothBranches() {
        // not instance -> keep value
        assertThat(Option.of("x").ifNotInstance(Integer.class).isPresent()).isTrue();
        // instance -> empty
        assertThat(Option.of("x").ifNotInstance(String.class).isEmpty()).isTrue();
    }

    @Test
    void ifPresentOrElse_mapperVsSupplier_branches() {
        // present -> mapper
        int len = Option.of("abc").ifPresentOrElse(String::length, () -> 0);
        assertThat(len).isEqualTo(3);

        // empty -> supplier
        int zero = Option.<String>empty().ifPresentOrElse(String::length, () -> 0);
        assertThat(zero).isEqualTo(0);
    }

    @Test
    void nullGuards_coverObjectsRequireNonNullBranches() {
        // Branch with NPE for null arguments
        assertThatThrownBy(() -> Option.of("x").apply(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.of("x").and(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.of("x").ifPresent(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.<String>empty().ifEmpty(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.of("x").ifEmptyOrElse(null, v -> {
        })).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.of("x").ifEmptyOrElse(() -> {
        }, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.of("x").map(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.<String>empty().flatMap(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.of("x").filter(null)).isInstanceOf(NullPointerException.class);

        // !: Optional is always not empty(NPE)
        assertThatThrownBy(() -> Option.<String>empty().toOptional().orElseThrow(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Option.of("x").isInstance(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.of("x").ifInstance(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.of("x").ifInstance(null, (Runnable) () -> {
        })).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.of("x").ifInstance(null, v -> {
        })).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.of("x").ifPresentOrElse((java.util.function.Function<String, Integer>) null, () -> 0))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.of("x").ifPresentOrElse((java.util.function.Supplier<Integer>) null, () -> 0))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.<String>empty().takeIf(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.<String>empty().takeUnless(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Option.<String>empty().orElseGet(null)).isInstanceOf(NullPointerException.class);
    }

}
