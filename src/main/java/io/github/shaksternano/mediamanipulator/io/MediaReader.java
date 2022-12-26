package io.github.shaksternano.mediamanipulator.io;

import java.io.Closeable;
import java.io.IOException;

public interface MediaReader<T> extends Closeable, Iterable<T> {

    double getFrameRate();

    int getFrameCount();

    int getAudioChannels();

    T getFrame(long timestamp) throws IOException;
}
