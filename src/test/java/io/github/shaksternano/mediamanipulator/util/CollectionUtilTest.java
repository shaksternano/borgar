package io.github.shaksternano.mediamanipulator.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

class CollectionUtilTest {

    @Test
    void keepEveryNthElement() {
        Assertions.assertAll(
                () -> Assertions.assertEquals(List.of(1, 3, 5), CollectionUtil.keepEveryNthElement(List.of(1, 2, 3, 4, 5), 2)),
                () -> Assertions.assertEquals(List.of(1), CollectionUtil.keepEveryNthElement(List.of(1, 2, 3, 4, 5), 10))
        );
    }

    @Test
    void intersection() {
        Assertions.assertAll(
                () -> Assertions.assertEquals(Set.of(1, 2), CollectionUtil.intersection(List.of(1, 2, 3), List.of(1, 2, 5, 7, 4))),
                () -> Assertions.assertEquals(Set.of(1, 2, 3), CollectionUtil.intersection(List.of(1, 2, 3), List.of(1, 2, 3)))
        );
    }

    @Test
    void extendLoop() {
        Assertions.assertAll(
                () -> Assertions.assertEquals(List.of(1, 2, 3, 1, 2, 3, 1), CollectionUtil.extendLoop(List.of(1, 2, 3), 7)),
                () -> Assertions.assertEquals(List.of(1), CollectionUtil.extendLoop(List.of(1), 1))
        );
    }
}
