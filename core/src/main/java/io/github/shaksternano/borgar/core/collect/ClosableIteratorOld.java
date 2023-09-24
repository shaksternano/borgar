package io.github.shaksternano.borgar.core.collect;

import io.github.shaksternano.borgar.core.util.AutoCloseableClosable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

public interface ClosableIteratorOld<T> extends Iterator<T>, Closeable {

    static <T> ClosableIteratorOld<T> wrap(Iterator<T> iterator) {
        if (iterator instanceof ClosableIteratorOld<T> closableIterator) {
            return closableIterator;
        }
        return new ClosableIteratorOld<>() {

            @Override
            public void remove() {
                iterator.remove();
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                iterator.forEachRemaining(action);
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }

            @Override
            public void close() throws IOException {
                if (iterator instanceof AutoCloseable closeable) {
                    AutoCloseableClosable.wrap(closeable).close();
                }
            }
        };
    }
}
