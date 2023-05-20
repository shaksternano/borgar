package io.github.shaksternano.mediamanipulator.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public interface ClosableIterator<T> extends Iterator<T>, Closeable {

    static <T> ClosableIterator<T> wrap(Iterator<T> iterator) {
        if (iterator instanceof ClosableIterator<T> closableIterator) {
            return closableIterator;
        }
        return new ClosableIterator<>() {

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
