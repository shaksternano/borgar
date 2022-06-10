package io.github.shaksternano.mediamanipulator.image.util;

import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Represents a frame with a duration
 */
public class AwtFrame implements Frame {

    /**
     * The image of the frame.
     */
    @Nullable
    private BufferedImage image;

    /**
     * The amount of time the frame is shown for in milliseconds.
     */
    private final int duration;

    /**
     * Creates a new {@code AwtFrame}.
     *
     * @param image    The image of the frame.
     * @param duration The amount of time the image is shown for in milliseconds.
     */
    @SuppressWarnings("NullableProblems")
    public AwtFrame(BufferedImage image, int duration) {
        this.image = image;
        this.duration = Math.max(duration, 1);
    }

    /**
     * Gets the image of the frame.
     *
     * @return The image of the frame.
     */
    @Override
    public BufferedImage getImage() {
        if (image == null) {
            throw new IllegalStateException();
        } else {
            return image;
        }
    }

    /**
     * Gets the amount of time the image is shown for in milliseconds.
     *
     * @return The amount of time the image is shown for in milliseconds.
     */
    @Override
    public int getDuration() {
        if (image == null) {
            throw new IllegalStateException();
        } else {
            return duration;
        }
    }

    @Override
    public void flush() {
        if (image == null) {
            throw new IllegalStateException();
        } else {
            image.flush();
            image = null;
        }
    }

    @Override
    public Frame copyWithDuration(int duration) {
        return new AwtFrame(image, duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(image, duration);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof AwtFrame other) {
            return Objects.equals(image, other.image)
                    && Objects.equals(duration, other.duration);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName() + "[");

        if (image == null) {
            builder.append("Flushed");
        } else {
            builder.append("Image: ")
                    .append(ImageUtil.imageToString(image))
                    .append(", Duration:")
                    .append(duration);
        }

        builder.append("]");
        return builder.toString();
    }
}
