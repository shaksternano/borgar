package io.github.shaksternano.mediamanipulator.io.mediareader;

import io.github.shaksternano.mediamanipulator.image.AudioFrame;
import io.github.shaksternano.mediamanipulator.io.MediaReaderFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public class NoAudioReader extends BaseMediaReader<AudioFrame> {

    public static final NoAudioReader INSTANCE = new NoAudioReader();

    public NoAudioReader() {
        super("N/A");
    }

    @Override
    public AudioFrame frame(long timestamp) {
        throw new UnsupportedOperationException("No audio available");
    }

    @Override
    public void close() {
    }

    @NotNull
    @Override
    public Iterator<AudioFrame> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public void forEach(Consumer<? super AudioFrame> action) {
    }

    @Override
    public Spliterator<AudioFrame> spliterator() {
        return Spliterators.emptySpliterator();
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
