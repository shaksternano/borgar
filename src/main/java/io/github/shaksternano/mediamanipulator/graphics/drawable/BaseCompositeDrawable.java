package io.github.shaksternano.mediamanipulator.graphics.drawable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class provides some default implementations for the {@code CompositeDrawable} interface.
 */
public abstract class BaseCompositeDrawable implements CompositeDrawable {

    /**
     * The parts of this composite drawable.
     */
    private final List<Drawable> parts = new ArrayList<>();

    /**
     * Gets the parts of this composite drawable.
     * @return The parts of this composite drawable.
     */
    protected List<Drawable> getParts() {
        return parts;
    }

    @Override
    public void addPart(Drawable part) {
        getParts().add(part);
    }
}
