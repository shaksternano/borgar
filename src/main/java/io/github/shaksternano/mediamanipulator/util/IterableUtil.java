package io.github.shaksternano.mediamanipulator.util;

public class IterableUtil {

    public static int hashElements(Iterable<?> iterable) {
        var hash = 1;
        for (var element : iterable) {
            hash = 31 * hash + element.hashCode();
        }
        return hash;
    }
}
