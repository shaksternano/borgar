package io.github.shaksternano.mediamanipulator.graphics.drawable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A {@code CompositeDrawable} that stores its parts in a {@link List}.
 */
public abstract class ListCompositeDrawable implements CompositeDrawable {

    /**
     * The parts of this composite drawable.
     */
    private final List<Drawable> parts = new ArrayList<>();

    @Override
    public int getFrameCount() {
        int maxFrameCount = 1;
        for (Drawable part : parts) {
            maxFrameCount = Math.max(maxFrameCount, part.getFrameCount());
        }

        return maxFrameCount;
    }

    @Override
    public boolean sameAsPreviousFrame() {
        for (Drawable part : parts) {
            if (!part.sameAsPreviousFrame()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the parts of this composite drawable.
     *
     * @return The parts of this composite drawable.
     */
    protected List<Drawable> getParts() {
        return parts;
    }

    @Override
    public void addPart(Drawable part) {
        parts.add(part);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(parts);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + parts;
    }
}
