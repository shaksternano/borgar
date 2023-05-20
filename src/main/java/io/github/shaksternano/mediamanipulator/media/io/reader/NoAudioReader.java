package io.github.shaksternano.mediamanipulator.media.io.reader;

import io.github.shaksternano.mediamanipulator.media.AudioFrame;
import io.github.shaksternano.mediamanipulator.media.io.MediaReaderFactory;
import io.github.shaksternano.mediamanipulator.util.ArrayUtil;
import io.github.shaksternano.mediamanipulator.util.ClosableIterator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class NoAudioReader extends BaseMediaReader<AudioFrame> {

    public static final NoAudioReader INSTANCE = new NoAudioReader();

    private NoAudioReader() {
        super("N/A");
    }

    @Override
    public AudioFrame frameAtTime(long timestamp) {
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
    public boolean contains(Object o) {
        return false;
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return ArrayUtil.createNewOrReuse(a, 0);
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return generator.apply(0);
    }

    @Override
    public ClosableIterator<AudioFrame> iterator() {
        return ClosableIterator.wrap(Collections.emptyIterator());
    }

    @Override
    public void forEach(Consumer<? super AudioFrame> action) {
    }

    @Override
    public Spliterator<AudioFrame> spliterator() {
        return Spliterators.emptySpliterator();
    }

    @Override
    public Stream<AudioFrame> stream() {
        return Stream.empty();
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
