package io.github.shaksternano.mediamanipulator.util;

import java.io.Closeable;
import java.io.IOException;

public record AutoCloseableClosable(AutoCloseable closeable) implements Closeable {

    @Override
    public void close() throws IOException {
        try {
            closeable.close();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
