package io.github.shaksternano.mediamanipulator.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains static methods for dealing with collections.
 */
public class CollectionUtil {

    /**
     * Removes every Nth element from the given {@link List}.
     *
     * @param collection The collection to remove elements from.
     * @param n          The n value.
     * @param cleanup    The code run on elements that are removed.
     * @param <T>        The type of the collection.
     * @return A new {@link List} with every Nth element removed.
     */
    public static <T> List<T> keepEveryNthElement(Collection<T> collection, int n, @Nullable Consumer<T> cleanup) {
        return keepEveryNthElement(collection.stream(), n, cleanup);
    }

    public static <T> List<T> keepEveryNthElement(Stream<T> stream, int n, @Nullable Consumer<T> cleanup) {
        return Streams
                .mapWithIndex(stream, AbstractMap.SimpleImmutableEntry::new)
                .filter(entry -> {
                    if (entry.getValue() % n == 0) {
                        return true;
                    } else {
                        if (cleanup != null) {
                            cleanup.accept(entry.getKey());
                        }

                        return false;
                    }
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static <T> Set<T> intersection(Collection<T> collection1, Collection<T> collection2) {
        Set<T> intersection = new HashSet<>(collection1);
        intersection.retainAll(collection2);
        return ImmutableSet.copyOf(intersection);
    }
}
