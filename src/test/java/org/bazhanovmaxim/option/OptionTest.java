package org.bazhanovmaxim.option;

import org.bazhanovmaxim.result.Try;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionTest {

    @Test
    void of_and_ofNullable_wrapValue() {
        assertThat(Option.of("A").get()).isEqualTo("A");
        assertThat(Option.ofNullable(null).isEmpty()).isTrue();
        assertThat(Option.ofNullable("B").isPresent()).isTrue();
    }

    @Test
    void empty_isSingletonAndEmpty() {
        Option<String> e1 = Option.empty();
        Option<String> e2 = Option.empty();
        assertThat(e1).isSameAs(e2);
        assertThat(e1.isEmpty()).isTrue();
        assertThat(e1.get()).isNull();
    }

    @Test
    void isPresent_isEmpty_isNotEmpty_workAsExpected() {
        assertThat(Option.of("x").isPresent()).isTrue();
        assertThat(Option.of("x").isEmpty()).isFalse();
        assertThat(Option.of("x").isNotEmpty()).isTrue();

        assertThat(Option.<String>empty().isPresent()).isFalse();
        assertThat(Option.<String>empty().isEmpty()).isTrue();
        assertThat(Option.<String>empty().isNotEmpty()).isFalse();
    }

    @Test
    void apply_runsSideEffectOnlyWhenPresent_andReturnsSameInstance() {
        AtomicBoolean called = new AtomicBoolean(false);
        Option<Integer> opt = Option.of(10);
        Option<Integer> same = opt.apply(v -> called.set(true));
        assertThat(called).isTrue();
        assertThat(same).isSameAs(opt);

        called.set(false);
        Option.<Integer>empty().apply(v -> called.set(true));
        assertThat(called).isFalse();
    }

    @Test
    void and_alwaysRunsSideEffect_andReturnsSameInstance() {
        AtomicBoolean called = new AtomicBoolean(false);
        Option<String> o = Option.of("x");
        Option<String> same = o.and(() -> called.set(true));
        assertThat(called).isTrue();
        assertThat(same).isSameAs(o);

        called.set(false);
        Option.<String>empty().and(() -> called.set(true));
        assertThat(called).isTrue();
    }

    @Test
    void ifPresent_runsOnlyWhenPresent() {
        AtomicBoolean called = new AtomicBoolean(false);
        Option.of("y").ifPresent(v -> called.set(true));
        assertThat(called).isTrue();

        called.set(false);
        Option.<String>empty().ifPresent(v -> called.set(true));
        assertThat(called).isFalse();
    }

    @Test
    void ifEmpty_runsOnlyWhenEmpty() {
        AtomicBoolean called = new AtomicBoolean(false);
        Option.<String>empty().ifEmpty(() -> called.set(true));
        assertThat(called).isTrue();

        called.set(false);
        Option.of("z").ifEmpty(() -> called.set(true));
        assertThat(called).isFalse();
    }

    @Test
    void ifEmptyOrElse_branchesCorrectly() {
        AtomicInteger box = new AtomicInteger(0);
        Option.of(7).ifEmptyOrElse(() -> box.set(-1), v -> box.set(v));
        assertThat(box.get()).isEqualTo(7);

        Option.<Integer>empty().ifEmptyOrElse(() -> box.set(-1), v -> box.set(v));
        assertThat(box.get()).isEqualTo(-1);
    }

    @Test
    void map_transformsWhenPresent_keepsEmptyOtherwise() {
        assertThat(Option.of("abc").map(String::length).get()).isEqualTo(3);
        assertThat(Option.<String>empty().map(String::length).isEmpty()).isTrue();
    }

    @Test
    void mapTo_appliesEvenForNull() {
        Integer len = Option.<String>empty().mapTo(s -> s == null ? 0 : s.length());
        assertThat(len).isEqualTo(0);
    }

    @Test
    void flatMap_flattensOptions_andPropagatesEmpty() {
        assertThat(
                Option.of("42")
                        .flatMap(s -> Option.of(Integer.parseInt(s)))
                        .get()
        ).isEqualTo(42);

        assertThat(
                Option.<String>empty()
                        .flatMap(s -> Option.of(1))
                        .isEmpty()
        ).isTrue();
    }

    @Test
    void flatMap_nullResultThrowsNpe() {
        assertThatThrownBy(() ->
                Option.of("x").flatMap(s -> null)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void filter_keepsOnlyWhenPredicateTrue() {
        assertThat(Option.of(6).filter(x -> x % 2 == 0).isPresent()).isTrue();
        assertThat(Option.of(5).filter(x -> x % 2 == 0).isEmpty()).isTrue();
        assertThat(Option.<Integer>empty().filter(x -> true).isEmpty()).isTrue();
    }

    @Test
    void orElseThrow_throwsOnEmpty() {
        assertThat(Option.of("ok").orElseThrow(IllegalStateException::new)).isEqualTo("ok");
        assertThatThrownBy(() -> Option.<String>empty().orElseThrow(IllegalStateException::new))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void toOptional_adaptsToJavaOptional() {
        Optional<String> opt = Option.of("x").toOptional();
        assertThat(opt).contains("x");

        Optional<String> empty = Option.<String>empty().toOptional();
        assertThat(empty).isEmpty();
    }

    @Test
    void isInstance_and_ifInstance_and_ifNotInstance_work() {
        assertThat(Option.of("x").isInstance(CharSequence.class)).isTrue();
        assertThat(Option.of(10).isInstance(String.class)).isFalse();

        assertThat(Option.of(10).ifInstance(Integer.class).get()).isEqualTo(10);
        assertThat(Option.of(10).ifInstance(String.class).isEmpty()).isTrue();

        assertThat(Option.of("x").ifNotInstance(Integer.class).isPresent()).isTrue();
        assertThat(Option.of("x").ifNotInstance(String.class).isEmpty()).isTrue();

        AtomicInteger len = new AtomicInteger(0);
        Option.of("abcd").ifInstance(CharSequence.class, cs -> len.set(cs.length()));
        assertThat(len.get()).isEqualTo(4);

        AtomicBoolean ran = new AtomicBoolean(false);
        Option.of("abcd").ifInstance(String.class, () -> ran.set(true));
        assertThat(ran.get()).isTrue();
    }

    @Test
    void ifPresentOrElse_overloads_work() {
        int size = Option.of("abc").ifPresentOrElse(String::length, () -> 0);
        assertThat(size).isEqualTo(3);

        String tag1 = Option.<String>empty().ifPresentOrElse(() -> "present", () -> "empty");
        String tag2 = Option.of("X").ifPresentOrElse(() -> "present", () -> "empty");
        assertThat(tag1).isEqualTo("empty");
        assertThat(tag2).isEqualTo("present");
    }

    @Test
    void runCatching_bridgesToTry_successAndFailure() {
        Try<Integer> ok = Option.of("42").runCatching(Integer::parseInt);
        assertThat(ok.isSuccess()).isTrue();
        assertThat(ok.getOrNull()).isEqualTo(42);

        Try<Integer> bad = Option.of("xx").runCatching(Integer::parseInt);
        assertThat(bad.isFailure()).isTrue();
        assertThat(bad.exceptionOrNull()).isInstanceOf(NumberFormatException.class);

        Try<Integer> emptyFail = Option.<String>empty().runCatching(Integer::parseInt);
        assertThat(emptyFail.isFailure()).isTrue();
        assertThat(emptyFail.exceptionOrNull()).isInstanceOf(NullPointerException.class);
    }

    @Test
    void equals_hashCode_toString_reasonable() {
        assertThat(Option.of("x")).isEqualTo(Option.of("x"));
        assertThat(Option.of("x").hashCode()).isEqualTo(Option.of("x").hashCode());
        assertThat(Option.<String>empty().toString()).isEqualTo("Option.empty");
        assertThat(Option.of("v").toString()).isEqualTo("Option[v]");
    }

    @Test
    void takeIf_onEmpty_staysEmpty() {
        assertThat(Option.<String>empty().takeIf(s -> true).isEmpty()).isTrue();
    }

    @Test
    void takeUnless_onEmpty_staysEmpty() {
        assertThat(Option.<String>empty().takeUnless(s -> false).isEmpty()).isTrue();
    }

    @Test
    void ifPresentOrElse_functionSupplier_nullValuePath() {
        // проверяем, что когда value == null, идём во второй путь (supplier)
        Integer val = Option.<String>empty().ifPresentOrElse(String::length, () -> 123);
        assertThat(val).isEqualTo(123);
    }

    @Test
    void ifInstance_falsePath_noAction() {
        java.util.concurrent.atomic.AtomicBoolean called = new java.util.concurrent.atomic.AtomicBoolean(false);
        Option.of(123).ifInstance(CharSequence.class, v -> called.set(true));
        assertThat(called).isFalse();
    }
}
