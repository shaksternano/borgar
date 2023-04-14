package io.github.shaksternano.mediamanipulator.image;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;

public interface DualImageProcessor<T> extends Closeable {

    BufferedImage transformImage(ImageFrame frame1, ImageFrame frame2, T constantData) throws IOException;

    /**
     * Calculates data that remains constant for all frames,
     * and so only needs to be calculated once.
     *
     * @param image1 The first frame of the first media.
     * @param image2 The first frame of the second media.
     * @return The data.
     * @throws IOException If an I/O error occurs.
     */
    T constantData(BufferedImage image1, BufferedImage image2) throws IOException;

    @Override
    default void close() throws IOException {
    }
}
