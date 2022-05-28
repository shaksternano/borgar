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
     * @param <E>        The type of the collection.
     * @return A new {@link List} with every Nth element removed.
     */
    public static <E> List<E> keepEveryNthElement(Collection<E> collection, int n, @Nullable Consumer<E> cleanup) {
        return keepEveryNthElement(collection.stream(), n, cleanup);
    }

    public static <E> List<E> keepEveryNthElement(Stream<E> stream, int n, @Nullable Consumer<E> cleanup) {
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

    public static <E> Set<E> intersection(Collection<E> collection1, Collection<E> collection2) {
        Set<E> intersection = new HashSet<>(collection1);
        intersection.retainAll(collection2);
        return ImmutableSet.copyOf(intersection);
    }

    public static <E> List<E> extendLoop(Collection<E> collection, int size) {
        if (collection.size() < size) {
            List<E> extended = new ArrayList<>(size);

            while (size - extended.size() >= collection.size()) {
                extended.addAll(collection);
            }

            if (extended.size() < size) {
                int remaining = size - extended.size();
                for (E element : collection) {
                    extended.add(element);
                    remaining--;

                    if (remaining == 0) {
                        break;
                    }
                }
            }

            return extended;
        } else {
            return new ArrayList<>(collection);
        }
    }
}
