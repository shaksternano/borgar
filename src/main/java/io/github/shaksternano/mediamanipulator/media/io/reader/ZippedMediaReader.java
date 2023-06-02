package io.github.shaksternano.mediamanipulator.media.io.reader;

import io.github.shaksternano.mediamanipulator.media.VideoFrame;
import io.github.shaksternano.mediamanipulator.util.ClosableIterator;
import io.github.shaksternano.mediamanipulator.util.MiscUtil;
import io.github.shaksternano.mediamanipulator.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;

public class ZippedMediaReader<A extends VideoFrame<?, A>, B extends VideoFrame<?, B>> extends BaseMediaReader<Pair<A, B>> {

    private final MediaReader<A> firstReader;
    private final MediaReader<B> secondReader;
    private final boolean firstControlling;
    @Nullable
    private ZippedMediaReader<A, B> reversed;

    public ZippedMediaReader(MediaReader<A> firstReader, MediaReader<B> secondReader) {
        this(firstReader, secondReader, decideIsFirstControlling(firstReader, secondReader));
    }

    public ZippedMediaReader(MediaReader<A> firstReader, MediaReader<B> secondReader, boolean firstControlling) {
        super(firstControlling ? firstReader.format() : secondReader.format());
        this.firstReader = firstReader;
        this.secondReader = secondReader;
        this.firstControlling = firstControlling;
        if (firstReader.isEmpty() || secondReader.isEmpty()) {
            frameDuration = 0;
            duration = 0;
            frameRate = 0;
            frameCount = 0;
        } else {
            this.frameDuration = firstControlling
                ? firstReader.frameDuration()
                : secondReader.frameDuration();
            duration = Math.max(firstReader.duration(), secondReader.duration());
            frameRate = firstControlling
                ? firstReader.frameRate()
                : secondReader.frameRate();
            frameCount = (int) (duration / frameDuration);
        }
        audioChannels = Math.max(firstReader.audioChannels(), secondReader.audioChannels());
        width = Math.max(firstReader.width(), secondReader.width());
        height = Math.max(firstReader.height(), secondReader.height());
    }

    @Override
    public Pair<A, B> readFrame(long timestamp) throws IOException {
        var firstFrame = firstReader.readFrame(timestamp);
        var secondFrame = secondReader.readFrame(timestamp);
        return new Pair<>(firstFrame, secondFrame);
    }

    @Override
    public Pair<A, B> first() throws IOException {
        var firstFrame = firstReader.first();
        var secondFrame = secondReader.first();
        return new Pair<>(firstFrame, secondFrame);
    }

    @Override
    public ZippedMediaReader<A, B> reversed() throws IOException {
        if (reversed == null) {
            reversed = new ZippedMediaReader<>(firstReader.reversed(), secondReader.reversed(), firstControlling);
            reversed.reversed = this;
        }
        return reversed;
    }

    @Override
    public ClosableIterator<Pair<A, B>> iterator() {
        return new ZippedMediaReaderIterator();
    }

    @Override
    public void close() throws IOException {
        MiscUtil.closeAll(firstReader, secondReader);
    }

    public boolean isFirstControlling() {
        return firstControlling;
    }

    private static boolean decideIsFirstControlling(MediaReader<?> firstReader, MediaReader<?> secondReader) {
        return firstReader.isAnimated() &&
            (!secondReader.isAnimated() || firstReader.frameDuration() <= secondReader.frameDuration());
    }

    private class ZippedMediaReaderIterator implements ClosableIterator<Pair<A, B>> {

        @Nullable
        private ClosableIterator<A> firstIterator;
        @Nullable
        private ClosableIterator<B> secondIterator;
        private int loops = 0;

        public ZippedMediaReaderIterator() {
            if (!firstReader.isEmpty() && !secondReader.isEmpty()) {
                if (firstControlling) {
                    firstIterator = firstReader.iterator();
                } else {
                    secondIterator = secondReader.iterator();
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (firstIterator != null) {
                return firstIterator.hasNext();
            } else if (secondIterator != null) {
                return secondIterator.hasNext();
            } else {
                return false;
            }
        }

        @Override
        public Pair<A, B> next() {
            try {
                if (firstIterator != null) {
                    var first = firstIterator.next();
                    var timestamp = first.timestamp() + (loops * firstReader.duration());
                    var second = secondReader.readFrame(timestamp);
                    if (!firstIterator.hasNext() && (timestamp + first.duration()) < secondReader.duration()) {
                        firstIterator.close();
                        firstIterator = firstReader.iterator();
                        loops++;
                    }
                    return new Pair<>(first, second);
                } else if (secondIterator != null) {
                    var second = secondIterator.next();
                    var timestamp = second.timestamp() + (loops * secondReader.duration());
                    var first = firstReader.readFrame(timestamp);
                    if (!secondIterator.hasNext() && (timestamp + second.duration()) < firstReader.duration()) {
                        secondIterator.close();
                        secondIterator = secondReader.iterator();
                        loops++;
                    }
                    return new Pair<>(first, second);
                } else {
                    throw new IllegalStateException("Both iterators are null");
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void close() throws IOException {
            MiscUtil.closeAll(firstIterator, secondIterator);
        }
    }
}
