package io.github.shaksternano.mediamanipulator.io.mediareader;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class FFmpegMediaReader<T> implements MediaReader<T> {

    protected final FFmpegFrameGrabber grabber;
    private final int frameCount;
    private final long duration;

    public FFmpegMediaReader(File input) throws IOException {
        this(new FFmpegFrameGrabber(input));
    }

    public FFmpegMediaReader(InputStream input) throws IOException {
        this(new FFmpegFrameGrabber(input));
    }

    private FFmpegMediaReader(FFmpegFrameGrabber grabber) throws IOException {
        this.grabber = grabber;
        grabber.start();
        int frameCount = 0;
        while (grabFrame() != null) {
            frameCount++;
        }
        this.frameCount = frameCount;
        duration = grabber.getTimestamp();
        grabber.setTimestamp(0);
    }

    @Nullable
    protected abstract Frame grabFrame() throws IOException;

    @Override
    public double getFrameRate() {
        if (frameCount <= 1) {
            return 30;
        } else {
            return grabber.getFrameRate();
        }
    }

    @Override
    public int getFrameCount() {
        return frameCount;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public double getFrameDuration() {
        return 1_000_000 / getFrameRate();
    }

    @Override
    public int getAudioChannels() {
        return grabber.getAudioChannels();
    }

    @Override
    public int getWidth() {
        return grabber.getImageWidth();
    }

    @Override
    public int getHeight() {
        return grabber.getImageHeight();
    }

    @Override
    public T getFrame(long timestamp) throws IOException {
        long circularTimestamp = timestamp % Math.max(getDuration(), 1);
        grabber.setTimestamp(circularTimestamp);
        T frame = getNextFrame();
        if (frame == null) {
            throw new NoSuchElementException("No frame at timestamp " + circularTimestamp);
        } else {
            return frame;
        }
    }

    @Override
    public long getTimestamp() {
        return grabber.getTimestamp();
    }

    @Override
    public void setTimestamp(long timestamp) throws IOException {
        grabber.setTimestamp(timestamp);
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