package io.github.shaksternano.mediamanipulator.io.mediareader;

import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

public interface MediaReader<T> extends Closeable, Iterable<T> {

    /**
     * Gets the frame rate of the media.
     *
     * @return The frame rate in frames per second.
     */
    double getFrameRate();

    int getFrameCount();

    /**
     * Gets the duration of time of the media.
     *
     * @return The duration in microseconds.
     */
    long getDuration();

    /**
     * Gets the duration of time of each frame.
     *
     * @return The duration of each frame in microseconds.
     */
    double getFrameDuration();

    int getAudioChannels();

    int getWidth();

    int getHeight();

    /**
     * Gets the frame at the given timestamp.
     * If the timestamp is larger than the duration of the media,
     * the reader with wrap around to the beginning.
     *
     * @param timestamp The timestamp in microseconds.
     * @return The frame at the given timestamp.
     * @throws IOException If an I/O error occurs.
     */
    T getFrame(long timestamp) throws IOException;

    @Nullable
    T getNextFrame() throws IOException;

    /**
     * Gets the timestamp the last read frame.
     *
     * @return The timestamp in microseconds.
     */
    long getTimestamp();

    /**
     * Sets the timestamp of the reader.
     * This is used to read a frame at a specific timestamp.
     *
     * @param timestamp The timestamp in microseconds.
     * @throws IOException If an I/O error occurs.
     */
    void setTimestamp(long timestamp) throws IOException;
}
