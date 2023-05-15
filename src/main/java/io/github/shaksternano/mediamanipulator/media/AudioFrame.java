package io.github.shaksternano.mediamanipulator.media;

import io.github.shaksternano.mediamanipulator.util.JavaCVUtil;
import org.bytedeco.javacv.Frame;

import java.util.Objects;

public record AudioFrame(
    Frame content,
    double duration,
    long timestamp
) implements VideoFrame<Frame> {

    @Override
    public AudioFrame transform(float speedMultiplier) {
        return transform(content, speedMultiplier);
    }

    @Override
    public AudioFrame transform(Frame newContent, float speedMultiplier) {
        newContent.sampleRate *= speedMultiplier;
        return new AudioFrame(
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
            JavaCVUtil.hashFrame(content)
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof AudioFrame other) {
            return Double.compare(other.duration, duration) == 0
                && timestamp == other.timestamp
                && JavaCVUtil.frameEquals(content, other.content());
        } else {
            return false;
        }
    }
}
