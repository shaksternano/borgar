package io.github.shaksternano.mediamanipulator.io.mediareader;

import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;

public class NoAudioReader extends BaseMediaReader<Frame> {

    public static final NoAudioReader INSTANCE = new NoAudioReader();

    @Nullable
    @Override
    public Frame getNextFrame() {
        return null;
    }

    @Override
    public long getTimestamp() {
        return 0;
    }

    @Override
    public void setTimestamp(long timestamp) {
    }

    @Override
    public void close() {
    }

    @NotNull
    @Override
    public Iterator<Frame> iterator() {
        return Collections.emptyIterator();
    }
}
