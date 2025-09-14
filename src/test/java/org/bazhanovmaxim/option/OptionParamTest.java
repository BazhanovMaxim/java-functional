package org.bazhanovmaxim.option;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class OptionParamTest {

    static @NotNull Stream<Arguments> valuesForOrElse() {
        return Stream.of(
                Arguments.of("null", "fb", "fb"),
                Arguments.of("val", "fb", "val")
        );
    }

    @ParameterizedTest
    @CsvSource({
            "abc,3",
            "x,1",
            "'',0"
    })
    void map_length(@NotNull String input, int expected) {
        Option<String> opt = Option.of(input.isEmpty() ? "" : input);
        assertThat(opt.map(String::length).get()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "4,true",
            "5,false",
            "10,true",
            "-2,true"
    })
    void filter_evenKeepsOnlyEven(int n, boolean kept) {
        Option<Integer> res = Option.of(n).filter(x -> x % 2 == 0);
        assertThat(res.isPresent()).isEqualTo(kept);
    }

    @ParameterizedTest
    @CsvSource({
            "abc,true",
            "a,false",
            "'',false"
    })
    void takeIf_lenGt1(String s, boolean present) {
        Option<String> res = Option.of(s).takeIf(v -> v.length() > 1);
        assertThat(res.isPresent()).isEqualTo(present);
    }

    @ParameterizedTest
    @CsvSource({
            "abc,false",
            "a,true",
            "'',true"
    })
    void takeUnless_lenGt1(String s, boolean present) {
        Option<String> res = Option.of(s).takeUnless(v -> v.length() > 1);
        assertThat(res.isPresent()).isEqualTo(present);
    }

    @ParameterizedTest
    @MethodSource("valuesForOrElse")
    void orElse_and_orElseGet(String value, String fallback, String expected) {
        Option<String> opt = "null".equals(value) ? Option.empty() : Option.of(value);
        assertThat(opt.orElse(fallback)).isEqualTo(expected);
        assertThat(opt.orElseGet(() -> fallback)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "java.lang.String,java.lang.CharSequence,true",
            "java.lang.Integer,java.lang.Number,true",
            "java.lang.Integer,java.lang.CharSequence,false"
    })
    void isInstance_matrix(String clazz, String target, boolean expected) throws Exception {
        Class<?> c1 = Class.forName(clazz);
        Class<?> c2 = Class.forName(target);
        Object v = c1 == String.class ? "x" : 1;
        assertThat(Option.of(v).isInstance(c2)).isEqualTo(expected);
    }
}
