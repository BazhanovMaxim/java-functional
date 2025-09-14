package org.bazhanovmaxim.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TryMoreBranchesTest {

    @Test
    void success_equals_hashcode_allBranches() {
        var a = Try.success(10);
        var b = Try.success(10);
        var c = Try.success(11);

        assertThat(a.equals(a)).isTrue();
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
        assertThat(a == null).isFalse();
        assertThat(a.equals("x")).isFalse();
    }

    @Test
    void failure_equals_hashcode_allBranches() {
        var a = Try.<Integer>failure(new IllegalStateException("x"));
        var b = Try.<Integer>failure(new IllegalStateException("x"));
        var c = Try.<Integer>failure(new IllegalArgumentException("y"));

        assertThat(a.equals(a)).isTrue();
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
        assertThat(a.equals(null)).isFalse();
        assertThat(a.equals("x")).isFalse();
    }
}
