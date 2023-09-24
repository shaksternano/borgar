package io.github.shaksternano.borgar.core.media.readerold;

import io.github.shaksternano.borgar.core.collect.ClosableIteratorOld;
import io.github.shaksternano.borgar.core.media.VideoFrameOld;
import io.github.shaksternano.borgar.core.util.MiscUtil;
import io.github.shaksternano.borgar.core.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;

public class ZippedMediaReader<A extends VideoFrameOld<?, A>, B extends VideoFrameOld<?, B>> extends BaseMediaReader<Pair<A, B>> {

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
        if (firstReader.loopCount() == 0 || secondReader.loopCount() == 0) {
            loopCount = 0;
        } else {
            loopCount = Math.max(firstReader.loopCount(), secondReader.loopCount());
        }
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
    public ClosableIteratorOld<Pair<A, B>> iterator() {
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

    private class ZippedMediaReaderIterator implements ClosableIteratorOld<Pair<A, B>> {

        @Nullable
        private ClosableIteratorOld<A> firstIterator;
        @Nullable
        private ClosableIteratorOld<B> secondIterator;
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
                    var timestamp = first.getTimestamp() + (loops * firstReader.duration());
                    var second = secondReader.readFrame(timestamp);
                    if (!firstIterator.hasNext() && (timestamp + first.getDuration()) < secondReader.duration()) {
                        firstIterator.close();
                        firstIterator = firstReader.iterator();
                        loops++;
                    }
                    return new Pair<>(first, second);
                } else if (secondIterator != null) {
                    var second = secondIterator.next();
                    var timestamp = second.getTimestamp() + (loops * secondReader.duration());
                    var first = firstReader.readFrame(timestamp);
                    if (!secondIterator.hasNext() && (timestamp + second.getDuration()) < firstReader.duration()) {
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
