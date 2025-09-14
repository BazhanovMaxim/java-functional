package org.bazhanovmaxim.result;

import org.bazhanovmaxim.any.Any;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TryEitherTest {

    @Test
    void success_toEither_isRight() {
        Any<Exception, Integer> e = Try.success(7).toEither();
        assertThat(e.isRight()).isTrue();
        assertThat(e.getRight()).isEqualTo(7);
    }

    @Test
    void failure_toEither_isLeft() {
        Exception ex = new IllegalStateException("boom");
        Any<Exception, Integer> e = Try.<Integer>failure(ex).toEither();
        assertThat(e.isLeft()).isTrue();
        assertThat(e.getLeft()).isSameAs(ex);
    }
}
