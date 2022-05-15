package io.github.shaksternano.mediamanipulator.util;

import java.awt.image.BufferedImage;

/**
 * Represents a frame with a duration
 */
public class DurationImage {

    /**
     * The minimum frame duration in milliseconds allowed on GIF files.
     */
    public static final int GIF_MINIMUM_FRAME_DURATION = 20;

    /**
     * The frame.
     */
    private BufferedImage image;

    /**
     * The amount of time the frame is shown for in milliseconds.
     */
    private int duration;

    /**
     * Creates a new DurationImage.
     *
     * @param image    The frame.
     * @param duration The amount of time the frame is shown for in milliseconds.
     */
    public DurationImage(BufferedImage image, int duration) {
        this.image = image;
        this.duration = duration;
    }

    /**
     * Gets the frame.
     *
     * @return The frame.
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Gets the amount of time the frame is shown for in milliseconds.
     *
     * @return The amount of time the frame is shown for in milliseconds.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the frame.
     *
     * @param image The frame to set.
     */
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    /**
     * Sets the amount of time the frame is shown for in milliseconds.
     *
     * @param duration The amount of time the frame is shown for in milliseconds to set.
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void incrementDuration() {
        incrementDuration(1);
    }

    public void incrementDuration(int amount) {
        duration += amount;
    }
}
