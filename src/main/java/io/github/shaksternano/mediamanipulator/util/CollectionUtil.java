package io.github.shaksternano.mediamanipulator.util;

import com.google.common.collect.Streams;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains static methods for dealing with collections.
 */
public class CollectionUtil {

    /**
     * Removes every Nth element from the given {@link List}.
     *
     * @param collection The collection to remove elements from.
     * @param n          The n value.
     * @param <T>        The type of the collection.
     * @return A new {@link List} with every Nth element removed.
     */
    public static <T> List<T> keepEveryNthElement(Collection<T> collection, int n) {
        return Streams
                .mapWithIndex(collection.stream(), AbstractMap.SimpleImmutableEntry::new)
                .filter(entry -> entry.getValue() % n == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
