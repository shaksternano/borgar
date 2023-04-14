package io.github.shaksternano.mediamanipulator.image;

public sealed interface VideoFrame<T> permits ImageFrame, AudioFrame {

    /**
     * The content of the frame.
     *
     * @return The content of the frame.
     */
    T content();

    /**
     * The duration of the frame in microseconds.
     *
     * @return The duration of the frame in microseconds.
     */
    double duration();

    /**
     * The timestamp of the frame in microseconds.
     *
     * @return The timestamp of the frame in microseconds.
     */
    long timestamp();
}