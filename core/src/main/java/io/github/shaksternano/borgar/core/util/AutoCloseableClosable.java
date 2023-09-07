package io.github.shaksternano.borgar.core.util;

import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

public record AutoCloseableClosable(@Nullable AutoCloseable closeable) implements Closeable {

    public static Closeable wrap(@Nullable AutoCloseable closeable) {
        if (closeable instanceof Closeable closeable1) {
            return closeable1;
        } else {
            return new AutoCloseableClosable(closeable);
        }
    }

    @Override
    public void close() throws IOException {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }
}
