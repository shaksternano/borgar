package io.github.shaksternano.borgar.util.collection;

public class IterableUtil {

    public static int hashElements(Iterable<?> iterable) {
        var hash = 1;
        for (var element : iterable) {
            hash = 31 * hash + element.hashCode();
        }
        return hash;
    }
}
