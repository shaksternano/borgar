package io.github.shaksternano.mediamanipulator.image;

import io.github.shaksternano.mediamanipulator.util.JavaCVUtil;
import org.bytedeco.javacv.Frame;

import java.util.Objects;

public record AudioFrame(
    Frame content,
    double duration,
    long timestamp
) implements VideoFrame<Frame> {

    @Override
    public int hashCode() {
        return Objects.hash(
            JavaCVUtil.hashFrame(content),
            duration,
            timestamp
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof AudioFrame other) {
            return duration == other.duration()
                && timestamp == other.timestamp()
                && JavaCVUtil.frameEquals(content, other.content());
        } else {
            return false;
        }
    }
}
