package io.github.shaksternano.mediamanipulator.util;

import java.awt.image.BufferedImage;

/**
 * Represents a frame of an animated image file.
 */
public class DelayedImage {

    /**
     * The minimum frame delay allowed on GIF files.
     */
    public static final int GIF_MINIMUM_DELAY = 20;

    /**
     * The frame.
     */
    private BufferedImage image;

    /**
     * The amount of time the frame is shown for in milliseconds.
     */
    private int delay;

    /**
     * Creates a new DelayedImage.
     *
     * @param image The frame.
     * @param delay The amount of time the frame is shown for in milliseconds.
     */
    public DelayedImage(BufferedImage image, int delay) {
        this.image = image;
        this.delay = delay;
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
    public int getDelay() {
        return delay;
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
     * @param delay The amount of time the frame is shown for in milliseconds to set.
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void incrementDelay() {
        incrementDelay(1);
    }

    public void incrementDelay(int amount) {
        delay += amount;
    }
}
