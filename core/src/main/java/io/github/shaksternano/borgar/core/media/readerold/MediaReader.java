package io.github.shaksternano.borgar.core.media.readerold;

import io.github.shaksternano.borgar.core.collect.ClosableIteratorOld;
import io.github.shaksternano.borgar.core.collect.ClosableSpliteratorOld;

import java.io.Closeable;
import java.io.IOException;

public interface MediaReader<E> extends Iterable<E>, Closeable {

    /**
     * Gets the frame rate of the media.
     *
     * @return The frame rate in frames per second.
     */
    double frameRate();

    int frameCount();

    boolean isEmpty();

    boolean isAnimated();

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

    int audioSampleRate();

    int audioBitrate();

    int width();

    int height();

    int loopCount();

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
    E readFrame(long timestamp) throws IOException;

    E first() throws IOException;

    MediaReader<E> reversed() throws IOException;

    @Override
    ClosableIteratorOld<E> iterator();

    @Override
    ClosableSpliteratorOld<E> spliterator();
}
