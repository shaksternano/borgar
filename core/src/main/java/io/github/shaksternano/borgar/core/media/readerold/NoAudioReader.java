package io.github.shaksternano.borgar.core.media.readerold;

import io.github.shaksternano.borgar.core.collect.ClosableIteratorOld;
import io.github.shaksternano.borgar.core.collect.ClosableSpliteratorOld;
import io.github.shaksternano.borgar.core.media.AudioFrameOld;
import io.github.shaksternano.borgar.core.media.MediaReaderFactoryOld;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Spliterators;
import java.util.function.Consumer;

public class NoAudioReader extends BaseMediaReader<AudioFrameOld> {

    public static final NoAudioReader INSTANCE = new NoAudioReader();

    private NoAudioReader() {
        super("N/A");
    }

    @Override
    public AudioFrameOld readFrame(long timestamp) {
        throw new UnsupportedOperationException("No audio available");
    }

    @Override
    public AudioFrameOld first() {
        throw new UnsupportedOperationException("No audio available");
    }

    @Override
    public MediaReader<AudioFrameOld> reversed() {
        return this;
    }

    @Override
    public ClosableIteratorOld<AudioFrameOld> iterator() {
        return ClosableIteratorOld.wrap(Collections.emptyIterator());
    }

    @Override
    public void forEach(Consumer<? super AudioFrameOld> action) {
    }

    @Override
    public ClosableSpliteratorOld<AudioFrameOld> spliterator() {
        return ClosableSpliteratorOld.wrap(Spliterators.emptySpliterator());
    }

    @Override
    public void close() {
    }

    public enum Factory implements MediaReaderFactoryOld<AudioFrameOld> {

        INSTANCE;

        @Override
        public MediaReader<AudioFrameOld> createReader(File media, String format) {
            return NoAudioReader.INSTANCE;
        }

        @Override
        public MediaReader<AudioFrameOld> createReader(InputStream media, String format) {
            return NoAudioReader.INSTANCE;
        }
    }
}
