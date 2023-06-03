package io.github.shaksternano.borgar.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

public interface ClosableIterator<T> extends Iterator<T>, Closeable {

    static <T> ClosableIterator<T> wrap(Iterator<T> iterator) {
        if (iterator instanceof ClosableIterator<T> closableIterator) {
            return closableIterator;
        }
        return new ClosableIterator<>() {

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
