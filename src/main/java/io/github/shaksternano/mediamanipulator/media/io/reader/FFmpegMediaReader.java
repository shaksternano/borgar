package io.github.shaksternano.mediamanipulator.media.io.reader;

import io.github.shaksternano.mediamanipulator.util.MiscUtil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class FFmpegMediaReader<E> extends BaseMediaReader<E> {

    protected final FFmpegFrameGrabber grabber;
    protected List<Closeable> toClose = new ArrayList<>();
    private boolean closed = false;

    public FFmpegMediaReader(File input, String format) throws IOException {
        this(new FFmpegFrameGrabber(input), format, null);
    }

    public FFmpegMediaReader(InputStream input, String format) throws IOException {
        this(new FFmpegFrameGrabber(input), format, input);
    }

    private FFmpegMediaReader(FFmpegFrameGrabber grabber, String format, @Nullable InputStream input) throws IOException {
        super(format);
        this.grabber = grabber;
        toClose.add(grabber);
        toClose.add(input);
        grabber.start();
        int frameCount = 0;
        Frame frame;
        while ((frame = grabFrame()) != null) {
            frameCount++;
            frame.close();
        }
        frameRate = grabber.getFrameRate();
        this.frameCount = frameCount;
        duration = grabber.getTimestamp();
        frameDuration = 1_000_000 / frameRate;
        audioChannels = grabber.getAudioChannels();
        width = grabber.getImageWidth();
        height = grabber.getImageHeight();
        grabber.setTimestamp(0);
    }

    @Nullable
    protected abstract Frame grabFrame() throws IOException;

    @Nullable
    protected abstract E getNextFrame() throws IOException;

    @Override
    public E frame(long timestamp) throws IOException {
        long circularTimestamp = timestamp % Math.max(duration(), 1);
        return frameNonCircular(circularTimestamp);
    }

    @Override
    public E first() throws IOException {
        return frameNonCircular(0);
    }

    private E frameNonCircular(long timestamp) throws IOException {
        grabber.setTimestamp(timestamp);
        E frame = getNextFrame();
        if (frame == null) {
            throw new NoSuchElementException("No frame at timestamp " + timestamp);
        } else {
            return frame;
        }
    }

    @Override
    public Iterator<E> iterator() {
        try {
            return new FFmpegMediaIterator();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        MiscUtil.closeAll(toClose);
    }

    private class FFmpegMediaIterator implements Iterator<E> {

        @Nullable
        E nextFrame;

        public FFmpegMediaIterator() throws IOException {
            grabber.setTimestamp(0);
            nextFrame = getNextFrame();
        }

        @Override
        public boolean hasNext() {
            return nextFrame != null;
        }

        @Override
        public E next() {
            if (nextFrame == null) {
                throw new NoSuchElementException();
            }
            E frame = nextFrame;
            try {
                nextFrame = getNextFrame();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return frame;
        }
    }
}
