package io.github.shaksternano.mediamanipulator.image.util;

import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

/**
 * Represents a frame with a duration
 */
public class AwtFrame implements Frame {

    /**
     * The image of the frame.
     */
    @Nullable
    private BufferedImage image;

    /**
     * The amount of time the frame is shown for in milliseconds.
     */
    private final int duration;

    /**
     * Creates a new Frame.
     *
     * @param image    The image of the frame.
     * @param duration The amount of time the image is shown for in milliseconds.
     */
    @SuppressWarnings("NullableProblems")
    public AwtFrame(BufferedImage image, int duration) {
        this.image = image;
        this.duration = duration;
    }

    /**
     * Gets the image of the frame.
     *
     * @return The image of the frame.
     */
    @Override
    public BufferedImage getImage() {
        if (image == null) {
            throw new IllegalStateException();
        } else {
            return image;
        }
    }

    /**
     * Gets the amount of time the image is shown for in milliseconds.
     *
     * @return The amount of time the image is shown for in milliseconds.
     */
    @Override
    public int getDuration() {
        if (image == null) {
            throw new IllegalStateException();
        } else {
            return duration;
        }
    }

    @Override
    public void flush() {
        if (image == null) {
            throw new IllegalStateException();
        } else {
            image.flush();
            image = null;
        }
    }
}
