package io.github.shaksternano.borgar.image.util;

import java.awt.image.BufferedImage;

public interface Frame {

    /**
     * The minimum frame duration in milliseconds allowed on GIF files.
     */
    int GIF_MINIMUM_FRAME_DURATION = 20;

    /**
     * Gets the image of the frame.
     *
     * @return The image of the frame.
     */
    BufferedImage getImage();

    /**
     * Gets the amount of time the image is shown for in milliseconds.
     *
     * @return The amount of time the image is shown for in milliseconds.
     */
    int getDuration();

    void flush();

    Frame copyWithDuration(int duration);
}
