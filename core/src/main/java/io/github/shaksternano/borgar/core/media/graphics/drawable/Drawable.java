package io.github.shaksternano.borgar.core.media.graphics.drawable;

import java.awt.*;
import java.io.Closeable;
import java.io.IOException;

/**
 * Represents an object that can be drawn on a {@link Graphics2D} object.
 */
public interface Drawable extends Closeable {

    /**
     * Draws the drawable.
     *
     * @param graphics  The graphics object to draw on.
     * @param x         The x coordinate of the top left corner of the drawable.
     * @param y         The y coordinate of the top left corner of the drawable.
     * @param timestamp The timestamp of the frame in microseconds.
     * @throws IOException If an I/O error occurs.
     */
    void draw(Graphics2D graphics, int x, int y, long timestamp) throws IOException;

    /**
     * Gets the width of the drawable.
     *
     * @param graphicsContext The graphics that the drawable will be drawn on.
     * @return The width of the drawable.
     */
    int getWidth(Graphics2D graphicsContext);

    /**
     * Gets the height of the drawable.
     *
     * @param graphicsContext The graphics that the drawable will be drawn on.
     * @return The height of the drawable.
     */
    int getHeight(Graphics2D graphicsContext);

    /**
     * Creates a new drawable from this one that has been resized to the given width.
     *
     * @param width The width to resize to.
     * @return A new drawable that has been resized to the given width.
     * @throws UnsupportedOperationException If the drawable cannot be resized by width.
     */
    Drawable resizeToWidth(int width);

    /**
     * Creates a new drawable from this one that has been resized to the given height.
     *
     * @param height The height to resize to.
     * @return A new drawable that has been resized to the given height.
     * @throws UnsupportedOperationException If the drawable cannot be resized by height.
     */
    Drawable resizeToHeight(int height);

    int getFrameCount();

    long getDuration();

    boolean sameAsPreviousFrame();

    @Override
    default void close() throws IOException {
    }
}
