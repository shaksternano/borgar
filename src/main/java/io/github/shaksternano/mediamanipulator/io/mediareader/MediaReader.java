package io.github.shaksternano.mediamanipulator.io.mediareader;

import java.io.Closeable;
import java.io.IOException;

public interface MediaReader<T> extends Closeable, Iterable<T> {

    /**
     * Gets the frame rate of the media.
     *
     * @return The frame rate in frames per second.
     */
    double frameRate();

    int frameCount();

    boolean animated();

    boolean empty();

    /**
     * Gets the duration of time of the media.
     *
     * @return The duration in microseconds.
     */
    long duration();

    /**
     * Gets the duration of time of each frame.
     *
     * @return The duration of each frame in microseconds.
     */
    double frameDuration();

    int audioChannels();

    int width();

    int height();

    String format();

    /**
     * Gets the frame at the given timestamp.
     * If the timestamp is larger than the duration of the media,
     * the reader will wrap around to the beginning.
     *
     * @param timestamp The timestamp in microseconds.
     * @return The frame at the given timestamp.
     * @throws IOException If an I/O error occurs.
     */
    T frame(long timestamp) throws IOException;
}
