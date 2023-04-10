package io.github.shaksternano.mediamanipulator.image;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;

public interface ImageProcessor<T> extends Closeable {

    BufferedImage transformImage(ImageFrame frame, T constantData) throws IOException;

    /**
     * Calculates data that remains constant for all frames,
     * and so only needs to be calculated once.
     *
     * @param image The first frame of the media.
     * @return The data.
     * @throws IOException If an I/O error occurs.
     */
    T constantData(BufferedImage image) throws IOException;

    @Override
    default void close() throws IOException {
    }
}
