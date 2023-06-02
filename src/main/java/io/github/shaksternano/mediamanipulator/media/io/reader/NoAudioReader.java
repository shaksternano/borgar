package io.github.shaksternano.mediamanipulator.media.io.reader;

import io.github.shaksternano.mediamanipulator.media.AudioFrame;
import io.github.shaksternano.mediamanipulator.media.io.MediaReaderFactory;
import io.github.shaksternano.mediamanipulator.util.ClosableIterator;
import io.github.shaksternano.mediamanipulator.util.ClosableSpliterator;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Spliterators;
import java.util.function.Consumer;

public class NoAudioReader extends BaseMediaReader<AudioFrame> {

    public static final NoAudioReader INSTANCE = new NoAudioReader();

    private NoAudioReader() {
        super("N/A");
    }

    @Override
    public AudioFrame readFrame(long timestamp) {
        throw new UnsupportedOperationException("No audio available");
    }

    @Override
    public AudioFrame first() {
        throw new UnsupportedOperationException("No audio available");
    }

    @Override
    public MediaReader<AudioFrame> reversed() {
        return this;
    }

    @Override
    public ClosableIterator<AudioFrame> iterator() {
        return ClosableIterator.wrap(Collections.emptyIterator());
    }

    @Override
    public void forEach(Consumer<? super AudioFrame> action) {
    }

    @Override
    public ClosableSpliterator<AudioFrame> spliterator() {
        return ClosableSpliterator.wrap(Spliterators.emptySpliterator());
    }

    @Override
    public void close() {
    }

    public enum Factory implements MediaReaderFactory<AudioFrame> {

        INSTANCE;

        @Override
        public MediaReader<AudioFrame> createReader(File media, String format) {
            return NoAudioReader.INSTANCE;
        }

        @Override
        public MediaReader<AudioFrame> createReader(InputStream media, String format) {
            return NoAudioReader.INSTANCE;
        }
    }
}
