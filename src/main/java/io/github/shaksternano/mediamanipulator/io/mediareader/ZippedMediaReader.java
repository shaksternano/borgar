package io.github.shaksternano.mediamanipulator.io.mediareader;

import io.github.shaksternano.mediamanipulator.image.VideoFrame;
import io.github.shaksternano.mediamanipulator.util.MiscUtil;
import io.github.shaksternano.mediamanipulator.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;

public class ZippedMediaReader<A extends VideoFrame<?>, B extends VideoFrame<?>> extends BaseMediaReader<Pair<A, B>> {

    private final MediaReader<A> firstReader;
    private final MediaReader<B> secondReader;
    private final boolean firstControlling;

    public ZippedMediaReader(MediaReader<A> firstReader, MediaReader<B> secondReader) {
        this(firstReader, secondReader, decideIsFirstControlling(firstReader, secondReader));
    }

    public ZippedMediaReader(MediaReader<A> firstReader, MediaReader<B> secondReader, boolean firstControlling) {
        super(firstReader.format());
        this.firstReader = firstReader;
        this.secondReader = secondReader;
        this.firstControlling = firstControlling;
        if (firstReader.empty() || secondReader.empty()) {
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
    public Pair<A, B> frame(long timestamp) throws IOException {
        var firstFrame = firstReader.frame(timestamp);
        var secondFrame = secondReader.frame(timestamp);
        return new Pair<>(firstFrame, secondFrame);
    }

    public boolean isFirstControlling() {
        return firstControlling;
    }

    private static boolean decideIsFirstControlling(MediaReader<?> firstReader, MediaReader<?> secondReader) {
        return firstReader.animated() &&
            (!secondReader.animated() || firstReader.frameDuration() <= secondReader.frameDuration());
    }

    @Override
    public void close() throws IOException {
        MiscUtil.closeAll(firstReader, secondReader);
    }

    @Override
    public Iterator<Pair<A, B>> iterator() {
        return new ZippedMediaReaderIterator();
    }

    private class ZippedMediaReaderIterator implements Iterator<Pair<A, B>> {

        @Nullable
        private Iterator<A> firstIterator;
        @Nullable
        private Iterator<B> secondIterator;
        private int loops = 0;

        public ZippedMediaReaderIterator() {
            if (!firstReader.empty() && !secondReader.empty()) {
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
                    var second = secondReader.frame(timestamp);
                    if (!firstIterator.hasNext() && (timestamp + first.duration()) < secondReader.duration()) {
                        firstIterator = firstReader.iterator();
                        loops++;
                    }
                    return new Pair<>(first, second);
                } else if (secondIterator != null) {
                    var second = secondIterator.next();
                    var timestamp = second.timestamp() + (loops * secondReader.duration());
                    var first = firstReader.frame(timestamp);
                    if (!secondIterator.hasNext() && (timestamp + second.duration()) < firstReader.duration()) {
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
    }
}
