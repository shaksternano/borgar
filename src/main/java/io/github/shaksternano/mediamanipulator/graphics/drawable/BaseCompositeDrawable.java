package io.github.shaksternano.mediamanipulator.graphics.drawable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj != null && getClass() == obj.getClass()) {
            BaseCompositeDrawable other = (BaseCompositeDrawable) obj;
            return Objects.equals(parts, other.parts);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(parts);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName() + "[");

        Iterator<Drawable> drawableIterator = parts.iterator();
        while (drawableIterator.hasNext()) {
            Drawable part = drawableIterator.next();
            builder.append(part);

            if (drawableIterator.hasNext()) {
                builder.append(", ");
            }
        }

        builder.append("]");
        return builder.toString();
    }
}
