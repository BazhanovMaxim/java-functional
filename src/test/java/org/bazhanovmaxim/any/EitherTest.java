package org.bazhanovmaxim.any;

import org.bazhanovmaxim.option.Option;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class EitherTest {

    @Test
    void isRight_isLeft_flags() {
        Any<String, Integer> r = new Right<>(1);
        Any<String, Integer> l = new Left<>("e");
        assertThat(r.isRight()).isTrue();
        assertThat(r.isLeft()).isFalse();
        assertThat(l.isLeft()).isTrue();
        assertThat(l.isRight()).isFalse();
    }

    @Test
    void getRight_getLeft() {
        Any<String, Integer> r = new Right<>(10);
        Any<String, Integer> l = new Left<>("oops");
        assertThat(r.getRight()).isEqualTo(10);
        assertThat(r.getLeft()).isNull();
        assertThat(l.getLeft()).isEqualTo("oops");
        assertThat(l.getRight()).isNull();
    }

    @Test
    void ifRight_ifLeft_runOnlyOnCorrectBranch() {
        AtomicInteger box = new AtomicInteger(0);
        new Right<String, Integer>(3).ifRight(box::set);
        assertThat(box.get()).isEqualTo(3);

        box.set(0);
        new Left<String, Integer>("e").ifRight(box::set);
        assertThat(box.get()).isEqualTo(0);

        new Left<String, Integer>("e").ifLeft(s -> box.set(42));
        assertThat(box.get()).isEqualTo(42);
    }

    @Test
    void map_affectsOnlyRight() {
        Any<Object, Integer> r = new Right<>(2).map(x -> x * 10);
        assertThat(r.isRight()).isTrue();
        assertThat(r.getRight()).isEqualTo(20);

        Any<String, Integer> l = new Left<String, Integer>("err").map(x -> x * 10);
        assertThat(l.isLeft()).isTrue();
        assertThat(l.getLeft()).isEqualTo("err");
    }

    @Test
    void flatMap_affectsOnlyRight() {
        Any<String, Integer> r = new Right<String, Integer>(5)
                .flatMap(x -> new Right<>(x + 1));
        assertThat(r.isRight()).isTrue();
        assertThat(r.getRight()).isEqualTo(6);

        Any<String, Integer> l = new Left<String, Integer>("e")
                .flatMap(x -> new Right<>(x + 1));
        assertThat(l.isLeft()).isTrue();
        assertThat(l.getLeft()).isEqualTo("e");
    }

    @Test
    void filterOrElse_onRight_keepsOrConvertsToLeft() {
        Any<String, Integer> ok = new Right<String, Integer>(10)
                .filterOrElse(x -> x > 5, "small");
        assertThat(ok.isRight()).isTrue();

        Any<String, Integer> bad = new Right<String, Integer>(3)
                .filterOrElse(x -> x > 5, "small");
        assertThat(bad.isLeft()).isTrue();
        assertThat(bad.getLeft()).isEqualTo("small");

        Any<String, Integer> stillLeft = new Left<String, Integer>("e")
                .filterOrElse(x -> true, "ignored");
        assertThat(stillLeft.isLeft()).isTrue();
        assertThat(stillLeft.getLeft()).isEqualTo("e");
    }

    @Test
    void exists_trueOnlyWhenRightSatisfiesPredicate() {
        assertThat(new Right<String, Integer>(9).exists(x -> x > 5)).isTrue();
        assertThat(new Right<String, Integer>(1).exists(x -> x > 5)).isFalse();
        assertThat(new Left<String, Integer>("e").exists(x -> x > 5)).isFalse();
    }

    @Test
    void fold_mergesBranches() {
        String s1 = new Right<String, Integer>(7)
                .fold(l -> "L:" + l, r -> "R:" + r);
        assertThat(s1).isEqualTo("R:7");

        String s2 = new Left<String, Integer>("oops")
                .fold(l -> "L:" + l, r -> "R:" + r);
        assertThat(s2).isEqualTo("L:oops");
    }

    @Test
    void forEach_runsOnlyOnCurrentBranch() {
        StringBuilder sb = new StringBuilder();
        new Right<String, Integer>(5).forEach(
                l -> sb.append("L"),
                r -> sb.append("R").append(r)
        );
        assertThat(sb.toString()).isEqualTo("R5");

        sb.setLength(0);
        new Left<String, Integer>("e").forEach(
                l -> sb.append("L").append(l),
                r -> sb.append("R")
        );
        assertThat(sb.toString()).isEqualTo("Le");
    }

    @Test
    void joinLeft_and_joinRight_behaveAsDefined() {
        Any<String, Integer> l1 = new Left<>("E1");
        Any<String, Integer> l2 = new Left<>("E2");
        Any<String, Integer> r1 = new Right<>(100);

        // Left.joinLeft(other) -> other
        assertThat(l1.joinLeft(l2)).isEqualTo(l2);
        assertThat(l1.joinLeft(r1)).isEqualTo(r1);

        // Right.joinLeft(other) -> this
        assertThat(r1.joinLeft(l2)).isEqualTo(r1);

        // Right.joinRight(other) -> other
        Any<String, Integer> r2 = new Right<>(200);
        assertThat(r1.joinRight(r2)).isEqualTo(r2);

        // Left.joinRight(other) -> this
        assertThat(l1.joinRight(r2)).isEqualTo(l1);
    }

    @Test
    void toOption_and_toOptional_convertRightOnly() {
        Option<Integer> some = new Right<String, Integer>(9).toOption();
        Option<Integer> none = new Left<String, Integer>("e").toOption();
        assertThat(some.isPresent()).isTrue();
        assertThat(none.isEmpty()).isTrue();

        Optional<Integer> js = new Right<String, Integer>(9).toOptional();
        Optional<Integer> jn = new Left<String, Integer>("e").toOptional();
        assertThat(js).contains(9);
        assertThat(jn).isEmpty();
    }

    @Test
    void equals_hashCode_toString_reasonable() {
        assertThat(new Right<String, Integer>(1))
                .isEqualTo(new Right<String, Integer>(1));
        assertThat(new Left<String, Integer>("e"))
                .isEqualTo(new Left<String, Integer>("e"));

        assertThat(new Right<String, Integer>(2).toString()).isEqualTo("Right(2)");
        assertThat(new Left<String, Integer>("x").toString()).isEqualTo("Left(x)");
    }

    @Test
    void swap_rightToLeft() {
        var s = new Right<String, Integer>(5).swap();
        assertThat(s.isLeft()).isTrue();
        assertThat(s.getLeft()).isEqualTo(5);
    }

    @Test
    void swap_leftToRight() {
        var s = new Left<String, Integer>("e").swap();
        assertThat(s.isRight()).isTrue();
        assertThat(s.getRight()).isEqualTo("e");
    }

    @Test
    void right_joinLeft_returnsThis() {
        var r = new Right<String, Integer>(1);
        assertThat(r.joinLeft(new Left<>("x"))).isSameAs(r);
    }

    @Test
    void left_joinRight_returnsThis() {
        var l = new Left<String, Integer>("e");
        assertThat(l.joinRight(new Right<>(2))).isSameAs(l);
    }
}
