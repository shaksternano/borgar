package io.github.shaksternano.mediamanipulator.io.mediareader;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

public interface MediaReader<E> extends Collection<E>, Closeable {

    /**
     * Gets the frame rate of the media.
     *
     * @return The frame rate in frames per second.
     */
    double frameRate();

    boolean animated();

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
    E frame(long timestamp) throws IOException;
}
