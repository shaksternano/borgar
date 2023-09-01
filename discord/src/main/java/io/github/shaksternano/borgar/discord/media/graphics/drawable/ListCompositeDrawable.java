package io.github.shaksternano.borgar.discord.media.graphics.drawable;

import io.github.shaksternano.borgar.discord.util.MiscUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
    public long getDuration() {
        return parts.stream()
            .max(Comparator.comparingLong(Drawable::getDuration))
            .map(Drawable::getDuration)
            .orElse(0L);
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
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (Objects.equals(getClass(), obj.getClass())) {
            ListCompositeDrawable other = (ListCompositeDrawable) obj;
            return Objects.equals(parts, other.parts);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + parts;
    }

    @Override
    public void close() throws IOException {
        MiscUtil.closeAll(parts);
    }
}
