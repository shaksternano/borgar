package io.github.shaksternano.mediamanipulator.util;

import com.google.common.collect.Streams;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
     * @param cleanup    The code run on elements that are removed.
     * @param <T>        The type of the collection.
     * @return A new {@link List} with every Nth element removed.
     */
    public static <T> List<T> keepEveryNthElement(Collection<T> collection, int n, @Nullable Consumer<T> cleanup) {
        return Streams
                .mapWithIndex(collection.stream(), AbstractMap.SimpleImmutableEntry::new)
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
}
