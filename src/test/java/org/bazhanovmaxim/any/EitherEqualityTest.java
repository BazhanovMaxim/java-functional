package org.bazhanovmaxim.any;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EitherEqualityTest {

    @Test
    void right_equals_hashcode_allBranches() {
        var a = new Right<String, Integer>(5);
        var b = new Right<String, Integer>(5);
        var c = new Right<String, Integer>(6);

        assertThat(a.equals(a)).isTrue();
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
        assertThat(a.equals(null)).isFalse();
        assertThat(a.equals("x")).isFalse();
    }

    @Test
    void left_equals_hashcode_allBranches() {
        var a = new Left<String, Integer>("e");
        var b = new Left<String, Integer>("e");
        var c = new Left<String, Integer>("f");

        assertThat(a.equals(a)).isTrue();
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
        assertThat(a.equals(null)).isFalse();
        assertThat(a.equals(123)).isFalse();
    }
}
