package io.github.shaksternano.mediamanipulator.graphics.drawable;

import java.awt.*;

/**
 * Represents an object that can be drawn on a {@link Graphics2D} object.
 */
public interface Drawable {

    /**
     * Draws the drawable.
     *
     * @param graphics The graphics object to draw on.
     * @param x        The x coordinate of the top left corner of the drawable.
     * @param y        The y coordinate of the top left corner of the drawable.
     */
    void draw(Graphics2D graphics, int x, int y);

    /**
     * Gets the width of the drawable.
     * @param graphicsContext The graphics that the drawable will be drawn on.
     * @return The width of the drawable.
     */
    int getWidth(Graphics2D graphicsContext);

    /**
     * Gets the height of the drawable.
     * @param graphicsContext The graphics that the drawable will be drawn on.
     * @return The height of the drawable.
     */
    int getHeight(Graphics2D graphicsContext);
}
