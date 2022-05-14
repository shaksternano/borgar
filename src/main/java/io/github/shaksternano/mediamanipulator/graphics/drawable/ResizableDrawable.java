package io.github.shaksternano.mediamanipulator.graphics.drawable;

/**
 * A drawable that can be resized.
 */
public interface ResizableDrawable extends Drawable {

    /**
     * Resizes the drawable to the given width.
     * @param width The width to resize to.
     */
    void resizeToWidth(int width);

    /**
     * Resizes the drawable to the given height.
     * @param height The height to resize to.
     */
    void resizeToHeight(int height);
}
