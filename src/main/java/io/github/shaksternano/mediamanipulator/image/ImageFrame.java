package io.github.shaksternano.mediamanipulator.image;

import java.awt.image.BufferedImage;
import java.util.Objects;

public record ImageFrame(
    BufferedImage content,
    double duration,
    long timestamp
) implements VideoFrame<BufferedImage> {

    @Override
    public int hashCode() {
        return Objects.hash(
            ImageUtil.hashImage(content),
            duration,
            timestamp
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof ImageFrame other) {
            return duration == other.duration()
                && timestamp == other.timestamp()
                && ImageUtil.imageEquals(content, other.content());
        } else {
            return false;
        }
    }
}
