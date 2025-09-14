package org.bazhanovmaxim.option;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class OptionBranchPadTest {

    @Test
    void isInstance_emptyIsAlwaysFalse() {
        assertThat(Option.<String>empty().isInstance(String.class)).isFalse();
    }

    @Test
    void ifInstance_returningOption_handlesEmptyAndMismatch() {
        // empty -> empty
        assertThat(Option.<Number>empty().ifInstance(Integer.class).isEmpty()).isTrue();
        // mismatch -> empty (мы это уже покрывали, но оставим для полноты)
        assertThat(Option.of(1.23).ifInstance(Integer.class).isEmpty()).isTrue();
        // match -> present
        assertThat(Option.of(123).ifInstance(Integer.class).isPresent()).isTrue();
    }

    @Test
    void ifInstance_consumer_doesNothingOnEmptyOrMismatch() {
        AtomicBoolean called = new AtomicBoolean(false);

        // empty
        Option.<String>empty().ifInstance(String.class, v -> called.set(true));
        assertThat(called).isFalse();

        // mismatch
        Option.of(123).ifInstance(String.class, v -> called.set(true));
        assertThat(called).isFalse();

        // match
        Option.of("ok").ifInstance(CharSequence.class, v -> called.set(true));
        assertThat(called).isTrue();
    }

    @Test
    void ifInstance_runnable_doesNothingOnEmptyOrMismatch() {
        AtomicBoolean called = new AtomicBoolean(false);

        // empty
        Option.<String>empty().ifInstance(String.class, () -> called.set(true));
        assertThat(called).isFalse();

        // mismatch
        Option.of(123).ifInstance(String.class, () -> called.set(true));
        assertThat(called).isFalse();

        // match
        Option.of("ok").ifInstance(String.class, () -> called.set(true));
        assertThat(called).isTrue();
    }

    @Test
    void ifNotInstance_emptyRemainsEmpty() {
        assertThat(Option.<String>empty().ifNotInstance(Object.class).isEmpty()).isTrue();
    }
}
