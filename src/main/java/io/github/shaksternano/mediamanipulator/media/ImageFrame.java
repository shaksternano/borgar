package io.github.shaksternano.mediamanipulator.media;

import java.awt.image.BufferedImage;
import java.util.Objects;

public record ImageFrame(
    BufferedImage content,
    double duration,
    long timestamp
) implements VideoFrame<BufferedImage> {

    @Override
    public ImageFrame transform(float speedMultiplier) {
        return transform(content, speedMultiplier);
    }

    @Override
    public ImageFrame transform(BufferedImage newContent, float speedMultiplier) {
        return new ImageFrame(
            newContent,
            duration / speedMultiplier,
            timestamp
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            duration,
            timestamp,
            ImageUtil.hashImage(content)
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof ImageFrame other) {
            return Double.compare(other.duration, duration) == 0
                && timestamp == other.timestamp
                && ImageUtil.imageEquals(content, other.content());
        } else {
            return false;
        }
    }
}
