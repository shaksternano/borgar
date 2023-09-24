package io.github.shaksternano.borgar.core.media.readerold;

import io.github.shaksternano.borgar.core.collect.ClosableIteratorOld;
import io.github.shaksternano.borgar.core.media.VideoFrameOld;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;

public class ConstantFrameDurationMediaReader<E extends VideoFrameOld<?, E>> extends BaseMediaReader<E> {

    private final MediaReader<E> reader;
    @Nullable
    private ConstantFrameDurationMediaReader<E> reversed;

    public ConstantFrameDurationMediaReader(MediaReader<E> reader, double frameDuration, long totalDuration) {
        super(reader.format());
        this.reader = reader;
        frameCount = (int) Math.ceil((double) totalDuration / frameDuration);
        duration = (long) (frameCount * frameDuration);
        frameRate = 1_000_000.0 / frameDuration;
        this.frameDuration = frameDuration;
        width = reader.width();
        height = reader.height();
    }

    public ConstantFrameDurationMediaReader(MediaReader<E> reader, double frameDuration) {
        this(reader, frameDuration, reader.duration());
    }

    @Override
    public E readFrame(long timestamp) throws IOException {
        var frameNumber = timestamp / (long) frameDuration;
        var newTimestamp = (long) (frameNumber * frameDuration);
        return reader.readFrame(timestamp).transform(frameDuration, newTimestamp);
    }

    @Override
    public MediaReader<E> reversed() throws IOException {
        if (reversed == null) {
            reversed = new ConstantFrameDurationMediaReader<>(reader.reversed(), frameDuration, duration);
            reversed.reversed = this;
        }
        return reversed;
    }

    @Override
    public ClosableIteratorOld<E> iterator() {
        return new Iterator();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private class Iterator implements ClosableIteratorOld<E> {

        private long currentTimestamp = 0;

        @Override
        public boolean hasNext() {
            return currentTimestamp < duration;
        }

        @Override
        public E next() {
            try {
                var frame = readFrame(currentTimestamp);
                currentTimestamp += frameDuration;
                return frame;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void close() {
        }
    }
}
