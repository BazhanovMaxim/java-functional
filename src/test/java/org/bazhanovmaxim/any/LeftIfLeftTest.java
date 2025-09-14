package org.bazhanovmaxim.any;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeftIfLeftTest {

    @Test
    void ifLeft_runsConsumerOnLeft() {
        AtomicReference<String> ref = new AtomicReference<>();
        new Left<String, Integer>("oops").ifLeft(ref::set);
        assertThat(ref).hasValue("oops");
    }

    @Test
    void ifLeft_nullConsumer_throwsNpe() {
        assertThatThrownBy(() -> new Left<String, Integer>("e").ifLeft(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void right_ifLeft_doesNothing() {
        AtomicReference<String> ref = new AtomicReference<>("init");
        new Right<String, Integer>(1).ifLeft(ref::set);
        assertThat(ref).hasValue("init"); // не изменилось
    }
}
