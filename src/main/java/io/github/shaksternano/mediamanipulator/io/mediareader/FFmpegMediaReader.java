package io.github.shaksternano.mediamanipulator.io.mediareader;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class FFmpegMediaReader<T> implements MediaReader<T> {

    protected final FFmpegFrameGrabber grabber;
    private final int frameCount;

    public FFmpegMediaReader(File input) throws IOException {
        grabber = new FFmpegFrameGrabber(input);
        grabber.start();
        int frameCount = 0;
        while (grabFrame() != null) {
            frameCount++;
        }
        this.frameCount = frameCount;
    }

    @Nullable
    protected abstract Frame grabFrame() throws IOException;

    @Nullable
    protected abstract T getNextFrame() throws IOException;

    @Override
    public double getFrameRate() {
        return grabber.getFrameRate();
    }

    @Override
    public int getFrameCount() {
        return frameCount;
    }

    @Override
    public int getAudioChannels() {
        return grabber.getAudioChannels();
    }

    @Override
    public T getFrame(long timestamp) throws IOException {
        long timestampBefore = grabber.getTimestamp();
        grabber.setTimestamp(timestamp);
        T frame = getNextFrame();
        grabber.setTimestamp(timestampBefore);
        return frame;
    }

    @Override
    public void close() throws IOException {
        grabber.close();
    }

    @Override
    public Iterator<T> iterator() {
        try {
            return new FFmpegMediaIterator();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private class FFmpegMediaIterator implements Iterator<T> {

        @Nullable
        T nextFrame;

        public FFmpegMediaIterator() throws IOException {
            grabber.setTimestamp(0);
            nextFrame = getNextFrame();
        }

        @Override
        public boolean hasNext() {
            return nextFrame != null;
        }

        @Override
        public T next() {
            if (nextFrame == null) {
                throw new NoSuchElementException();
            }
            T frame = nextFrame;
            try {
                nextFrame = getNextFrame();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return frame;
        }
    }
}
