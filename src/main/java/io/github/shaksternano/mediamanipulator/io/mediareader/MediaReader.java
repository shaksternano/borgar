package io.github.shaksternano.mediamanipulator.io.mediareader;

import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

public interface MediaReader<T> extends Closeable, Iterable<T> {

    /**
     * Gets the frame rate of the media.
     * @return The frame rate in frames per second.
     */
    double getFrameRate();

    int getFrameCount();

    /**
     * Gets the duration of time of the media.
     * @return The duration in microseconds.
     */
    long getDuration();

    /**
     * Gets the duration of time of each frame.
     * @return The duration of each frame in microseconds.
     */
    double getFrameDuration();

    int getAudioChannels();

    int getWidth();

    int getHeight();

    T getFrame(long timestamp) throws IOException;

    @Nullable
    T getNextFrame() throws IOException;

    long getTimestamp();

    void setTimestamp(long timestamp) throws IOException;
}
