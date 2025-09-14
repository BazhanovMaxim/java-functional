package org.bazhanovmaxim.any;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EitherParamTest {

    @ParameterizedTest
    @CsvSource({
            "RIGHT,2,20",
            "RIGHT,0,0"
    })
    void bimap_onRight_mapsRight(String side, int value, int expected) {
        Any<String, Integer> e = new Right<>(value);
        Any<Integer, Integer> r = e.bimap(String::length, x -> x * 10);
        assertThat(r.isRight()).isTrue();
        assertThat(r.getRight()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "LEFT,err,3"
    })
    void bimap_onLeft_mapsLeft(String side, String err, int expectedLeftLen) {
        Any<String, Integer> e = new Left<>(err);
        Any<Integer, Integer> r = e.bimap(String::length, x -> x * 10);
        assertThat(r.isLeft()).isTrue();
        assertThat(r.getLeft()).isEqualTo(expectedLeftLen);
    }

    @ParameterizedTest
    @CsvSource({
            "RIGHT,5,true,false",
            "LEFT,0,false,true"
    })
    void swap_flipsBranch(@NotNull String side, int value, boolean expectLeft, boolean expectRight) {
        Any<String, Integer> e = side.equals("RIGHT") ? new Right<>(value) : new Left<>("e");
        Any<Integer, String> s = e.swap();
        assertThat(s.isLeft()).isEqualTo(expectLeft);
        assertThat(s.isRight()).isEqualTo(expectRight);
    }

    @ParameterizedTest
    @CsvSource({
            "RIGHT,2,2",
            "LEFT,0,2"
    })
    void mapLeft_rightUnchanged_leftMapped(@NotNull String side, int value, int expectedLeftLen) {
        Any<String, Integer> e = side.equals("RIGHT") ? new Right<>(value) : new Left<>("xx");
        Any<Integer, Integer> r = e.mapLeft(String::length);
        if (side.equals("RIGHT")) {
            assertThat(r.isRight()).isTrue();
            assertThat(r.getRight()).isEqualTo(value);
        } else {
            assertThat(r.isLeft()).isTrue();
            assertThat(r.getLeft()).isEqualTo(expectedLeftLen);
        }
    }
}
