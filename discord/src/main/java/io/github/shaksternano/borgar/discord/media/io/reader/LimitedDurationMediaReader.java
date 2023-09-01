package io.github.shaksternano.borgar.discord.media.io.reader;

import io.github.shaksternano.borgar.discord.media.VideoFrame;
import io.github.shaksternano.borgar.discord.util.collect.ClosableIterator;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.NoSuchElementException;

public class LimitedDurationMediaReader<E extends VideoFrame<?, E>> extends BaseMediaReader<E> {

    private final MediaReader<E> reader;
    @Nullable
    private LimitedDurationMediaReader<E> reversed;

    public LimitedDurationMediaReader(MediaReader<E> reader, long maxDuration) throws IOException {
        super(reader.format());
        this.reader = reader;
        var readerInfo = readerInfo(reader, maxDuration);
        frameCount = readerInfo.frameCount();
        this.duration = readerInfo.duration();
        frameDuration = (double) duration / frameCount;
        frameRate = 1_000_000 / frameDuration;
        width = reader.width();
        height = reader.height();
        loopCount = reader.loopCount();
    }

    private ReaderInfo readerInfo(MediaReader<E> reader, long maxDuration) throws IOException {
        try (var iterator = reader.iterator()) {
            var frameCount = 0;
            var duration = 0L;
            while (iterator.hasNext()) {
                var frame = iterator.next();
                var newDuration = duration + (long) frame.duration();
                if (newDuration > maxDuration) {
                    if (frameCount == 0) {
                        return new ReaderInfo(1, newDuration);
                    } else {
                        return new ReaderInfo(frameCount, duration);
                    }
                }
                frameCount++;
                duration = newDuration;
            }
        }
        return new ReaderInfo(reader);
    }

    @Override
    public E readFrame(long timestamp) throws IOException {
        var circularTimestamp = timestamp % Math.max(duration(), 1);
        return reader.readFrame(circularTimestamp);
    }

    @Override
    public MediaReader<E> reversed() throws IOException {
        if (reversed == null) {
            reversed = new LimitedDurationMediaReader<>(reader.reversed(), duration);
            reversed.reversed = this;
        }
        return reversed;
    }

    @Override
    public ClosableIterator<E> iterator() {
        return new Iterator();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private class Iterator implements ClosableIterator<E> {

        private final ClosableIterator<E> delegate = reader.iterator();
        private long currentTimestamp = 0;

        @Override
        public boolean hasNext() {
            return currentTimestamp < duration && delegate.hasNext();
        }

        @Override
        public E next() {
            if (currentTimestamp >= duration) {
                throw new NoSuchElementException("Iterator has no more elements");
            }
            var frame = delegate.next();
            currentTimestamp += frame.duration();
            return frame;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    private record ReaderInfo(int frameCount, long duration) {

        private ReaderInfo(MediaReader<?> reader) {
            this(reader.frameCount(), reader.duration());
        }
    }
}
