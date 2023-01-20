package io.github.shaksternano.mediamanipulator.io.mediareader;

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
     * Gets the length of time of the media.
     * @return The length in microseconds.
     */
    long getLength();

    /**
     * Gets the length of time of each frame.
     * @return The length of each frame in microseconds.
     */
    double getFrameLength();

    int getAudioChannels();

    T getFrame(long timestamp) throws IOException;
}
