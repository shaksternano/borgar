package io.github.shaksternano.borgar.discord.util.collect;

import java.io.Closeable;
import java.util.Iterator;
import java.util.Spliterator;

public interface ClosableSpliterator<T> extends Spliterator<T>, Closeable {

    static <T> ClosableSpliterator<T> wrap(Spliterator<T> spliterator) {
        if (spliterator instanceof ClosableSpliterator<T> closableSpliterator) {
            return closableSpliterator;
        }
        return new ClosableSpliteratorImpl<>(spliterator);
    }

    static <T> ClosableSpliterator<T> create(Iterator<T> iterator, long size, int characteristics) {
        return new ClosableSpliteratorImpl<>(iterator, size, characteristics);
    }
}
