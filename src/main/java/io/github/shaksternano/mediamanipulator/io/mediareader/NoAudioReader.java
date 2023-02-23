package io.github.shaksternano.mediamanipulator.io.mediareader;

import io.github.shaksternano.mediamanipulator.io.MediaReaderFactory;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;

public class NoAudioReader extends BaseMediaReader<Frame> {

    public static final NoAudioReader INSTANCE = new NoAudioReader();

    @Override
    public Frame getFrame(long timestamp) throws IOException {
        throw new UnsupportedOperationException("No audio available");
    }

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

    public enum Factory implements MediaReaderFactory<Frame> {

        INSTANCE;

        @Override
        public MediaReader<Frame> createReader(File media) {
            return NoAudioReader.INSTANCE;
        }

        @Override
        public MediaReader<Frame> createReader(InputStream media) {
            return NoAudioReader.INSTANCE;
        }
    }
}
