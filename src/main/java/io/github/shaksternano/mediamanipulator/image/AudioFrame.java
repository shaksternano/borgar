package io.github.shaksternano.mediamanipulator.image;

import org.bytedeco.javacv.Frame;

public record AudioFrame(
    Frame content,
    double duration,
    long timestamp
) implements VideoFrame<Frame> {
}
