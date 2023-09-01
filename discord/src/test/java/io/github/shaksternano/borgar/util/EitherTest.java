package io.github.shaksternano.borgar.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.function.Function;

public class EitherTest {

    @Test
    public void testLeft() {
        Either<String, Integer> either = Either.left("https://youtu.be/dQw4w9WgXcQ");
        Assertions.assertTrue(either.isLeft());
        Assertions.assertFalse(either.isRight());
        Assertions.assertEquals("https://youtu.be/dQw4w9WgXcQ", either.left());
        Assertions.assertThrows(NoSuchElementException.class, either::right);
        Assertions.assertEquals("https", either.mapBoth(s -> s.substring(0, 5), i -> i * 2).left());
        Assertions.assertEquals("https", either.mapLeft(s -> s.substring(0, 5)).left());
        Assertions.assertEquals("https://youtu.be/dQw4w9WgXcQ", either.mapRight(i -> i * 2).left());
        Assertions.assertEquals(28, either.map(String::length, Function.identity()));
        Assertions.assertTrue(either.maybeL().isPresent());
    }

    @Test
    public void testRight() {
        Either<String, Integer> either = Either.right(5);
        Assertions.assertFalse(either.isLeft());
        Assertions.assertTrue(either.isRight());
        Assertions.assertEquals(5, either.right());
        Assertions.assertThrows(NoSuchElementException.class, either::left);
        Assertions.assertEquals(10, either.mapBoth(s -> s.substring(0, 5), i -> i * 2).right());
        Assertions.assertEquals(5, either.mapLeft(s -> s.substring(0, 5)).right());
        Assertions.assertEquals(10, either.mapRight(i -> i * 2).right());
        Assertions.assertEquals(5, either.map(String::length, Function.identity()));
        Assertions.assertTrue(either.maybeR().isPresent());
    }
}
