package io.github.shaksternano.borgar.core.collect;

import java.io.Closeable;
import java.util.Iterator;
import java.util.Spliterator;

public interface ClosableSpliteratorOld<T> extends Spliterator<T>, Closeable {

    static <T> ClosableSpliteratorOld<T> wrap(Spliterator<T> spliterator) {
        if (spliterator instanceof ClosableSpliteratorOld<T> closableSpliterator) {
            return closableSpliterator;
        }
        return new ClosableSpliteratorImpl<>(spliterator);
    }

    static <T> ClosableSpliteratorOld<T> create(Iterator<T> iterator, long size, int characteristics) {
        return new ClosableSpliteratorImpl<>(iterator, size, characteristics);
    }
}
