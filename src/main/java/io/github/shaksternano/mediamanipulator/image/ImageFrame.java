package io.github.shaksternano.mediamanipulator.image;

import java.awt.image.BufferedImage;

public record ImageFrame(
    BufferedImage content,
    double duration,
    long timestamp
) implements VideoFrame<BufferedImage> {
}
