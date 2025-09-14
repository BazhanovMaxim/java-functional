package org.bazhanovmaxim.result;

import org.bazhanovmaxim.option.Option;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TryTest {

    @Test
    void constructors_and_predicates() {
        Try<Integer> ok = Try.success(1);
        Try<Integer> ko = Try.failure(new IllegalStateException("boom"));

        assertThat(ok.isSuccess()).isTrue();
        assertThat(ok.isFailure()).isFalse();
        assertThat(ko.isSuccess()).isFalse();
        assertThat(ko.isFailure()).isTrue();
    }

    @Test
    void accessors_getOrNull_and_exceptionOrNull() {
        Try<String> ok = Try.success("x");
        Try<String> ko = Try.failure(new RuntimeException("r"));

        assertThat(ok.getOrNull()).isEqualTo("x");
        assertThat(ok.exceptionOrNull()).isNull();

        assertThat(ko.getOrNull()).isNull();
        assertThat(ko.exceptionOrNull()).isInstanceOf(RuntimeException.class);
    }

    @Test
    void map_transformsOnlySuccess() {
        Try<Integer> ok = Try.success(5).map(x -> x + 1);
        Try<Integer> ko = Try.<Integer>failure(new RuntimeException()).map(x -> x + 1);

        assertThat(ok.isSuccess()).isTrue();
        assertThat(ok.getOrNull()).isEqualTo(6);

        assertThat(ko.isFailure()).isTrue();
        assertThat(ko.exceptionOrNull()).isInstanceOf(RuntimeException.class);
    }

    @Test
    void flatMap_chainsOnlySuccess() {
        Try<Integer> ok =
                Try.success("42").flatMap(s -> {
                    try {
                        return Try.success(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        return Try.failure(e);
                    }
                });
        assertThat(ok.isSuccess()).isTrue();
        assertThat(ok.getOrNull()).isEqualTo(42);

        Try<Integer> ko =
                Try.<String>failure(new IllegalStateException("x"))
                        .flatMap(s -> Try.success(1));
        assertThat(ko.isFailure()).isTrue();
        assertThat(ko.exceptionOrNull()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onSuccess_onFailure_runSideEffects() {
        StringBuilder sb = new StringBuilder();
        Try.success(7)
                .onSuccess(v -> sb.append("ok"))
                .onFailure(e -> sb.append("fail"));
        assertThat(sb.toString()).isEqualTo("ok");

        sb.setLength(0);
        Try.failure(new RuntimeException())
                .onSuccess(v -> sb.append("ok"))
                .onFailure(e -> sb.append("fail"));
        assertThat(sb.toString()).isEqualTo("fail");
    }

    @Test
    void getOrElse_and_recover() {
        int v1 = Try.<Integer>failure(new Exception()).getOrElse(() -> 0);
        int v2 = Try.<Integer>failure(new Exception("e")).recover(ex -> 5);
        int v3 = Try.success(10).getOrElse(() -> -1);
        int v4 = Try.success(10).recover(ex -> -1);

        assertThat(v1).isEqualTo(0);
        assertThat(v2).isEqualTo(5);
        assertThat(v3).isEqualTo(10);
        assertThat(v4).isEqualTo(10);
    }

    @Test
    void fold_mergesBranches() {
        String s = Try.success(2).fold(
                ex -> "fail: " + ex.getMessage(),
                v -> "ok: " + v
        );
        assertThat(s).isEqualTo("ok: 2");

        String t = Try.<Integer>failure(new IllegalArgumentException("bad")).fold(
                ex -> "fail: " + ex.getClass().getSimpleName(),
                v -> "ok: " + v
        );
        assertThat(t).isEqualTo("fail: IllegalArgumentException");
    }

    @Test
    void getOrThrow_returnsValue_orThrowsRuntimeWrapping() {
        assertThat(Try.success(3).getOrThrow()).isEqualTo(3);

        assertThatThrownBy(() ->
                Try.<Integer>failure(new Exception("checked")).getOrThrow()
        ).isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(Exception.class);

        assertThatThrownBy(() ->
                Try.<Integer>failure(new IllegalStateException("boom")).getOrThrow()
        ).isInstanceOf(IllegalStateException.class) // rethrown as-is for RuntimeException
                .hasMessage("boom");
    }

    @Test
    void flatMap_nullMapperResult_throwsNPE() {
        assertThatThrownBy(() ->
                Try.success(1).flatMap(v -> null)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void toOption_convertsSuccessToSome_andFailureToEmpty() {
        Option<Integer> some = Try.success(5).toOption();
        Option<Integer> none = Try.<Integer>failure(new RuntimeException()).toOption();

        assertThat(some.isPresent()).isTrue();
        assertThat(none.isEmpty()).isTrue();
    }

    @Test
    void equality_hashCode_and_toString_reasonable() {
        assertThat(Try.success(10)).isEqualTo(Try.success(10));
        var s2 = Try.<Integer>failure(new RuntimeException("x")).toString();
        assertThat(s2).startsWith("Failure(");
        assertThat(s2).matches("^Failure\\(java\\.lang\\.[A-Za-z]+Exception: x\\).*");
        assertThat(Try.success(1).toString()).startsWith("Success(");
        assertThat(Try.<Integer>failure(new RuntimeException("r")).toString()).startsWith("Failure(");
    }
}
