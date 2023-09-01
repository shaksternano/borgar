package io.github.shaksternano.borgar.discord.media;

public sealed interface VideoFrame<T, V extends VideoFrame<T, V>> extends Comparable<VideoFrame<?, ?>> permits ImageFrame, AudioFrame {

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

    V transform(T newContent);

    V transform(float speedMultiplier);

    V transform(T newContent, float speedMultiplier);

    V transform(double duration, long timestamp);

    @Override
    default int compareTo(VideoFrame<?, ?> o) {
        return Long.compare(timestamp(), o.timestamp());
    }
}
