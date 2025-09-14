package org.bazhanovmaxim.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TryEqualsPadTest {

    @Test
    void success_notEquals_failure() {
        assertThat(Try.success(1)).isNotEqualTo(Try.<Integer>failure(new RuntimeException("x")));
        assertThat(Try.<Integer>failure(new RuntimeException("x"))).isNotEqualTo(Try.success(1));
    }

    @Test
    void failure_notEquals_sameTypeDifferentMessage() {
        var a = Try.<Integer>failure(new IllegalStateException("x"));
        var b = Try.<Integer>failure(new IllegalStateException("y"));
        assertThat(a).isNotEqualTo(b);
    }
}
