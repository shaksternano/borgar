package io.github.shaksternano.mediamanipulator.media.io.Imageprocessor;

import java.io.Closeable;
import java.io.IOException;

public interface ImageProcessor extends Closeable {

    default float speed() {
        return 1;
    }

    @Override
    default void close() throws IOException {
    }
}
