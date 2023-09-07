package io.github.shaksternano.borgar.core.media.graphics.drawable;

/**
 * A drawable that is composed of multiple drawables. The way the drawables
 * are combined when drawn is up to the implementation of this interface.
 */
public interface CompositeDrawable extends Drawable {

    /**
     * Adds a part to the composite drawable.
     *
     * @param part The part to add.
     */
    void addPart(Drawable part);
}
