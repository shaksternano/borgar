package io.github.shaksternano.mediamanipulator.media.io.Imageprocessor;

import io.github.shaksternano.mediamanipulator.media.ImageFrame;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface SingleImageProcessor<T> extends ImageProcessor {

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
}
