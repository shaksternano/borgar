package io.github.shaksternano.mediamanipulator.io;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface ImageProcessor<T> {

    BufferedImage transformImage(BufferedImage image, T extraData);

    T globalData(BufferedImage image) throws IOException;

    default boolean isDone(boolean readAllFrames) {
        return readAllFrames;
    }
}
