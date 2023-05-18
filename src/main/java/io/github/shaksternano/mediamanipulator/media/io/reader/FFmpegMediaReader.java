package io.github.shaksternano.mediamanipulator.media.io.reader;

import io.github.shaksternano.mediamanipulator.util.Either;
import io.github.shaksternano.mediamanipulator.util.MiscUtil;
import org.apache.commons.io.IOUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract sealed class FFmpegMediaReader<E> extends BaseMediaReader<E> permits FFmpegImageReader, FFmpegAudioReader {

    private final Either<File, byte[]> input;
    protected final FFmpegFrameGrabber grabber;
    protected final List<Closeable> toClose = new ArrayList<>();
    private boolean closed = false;

    public FFmpegMediaReader(File input, String format) throws IOException {
        this(Either.left(input), format);
    }

    public FFmpegMediaReader(InputStream input, String format) throws IOException {
        this(Either.right(input), format);
    }

    private FFmpegMediaReader(Either<File, InputStream> input, String format) throws IOException {
        super(format);
        this.input = input.mapRight(inputStream -> {
            try (inputStream) {
                return IOUtils.toByteArray(inputStream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        this.grabber = createGrabber();
        toClose.add(grabber);
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

    private FFmpegFrameGrabber createGrabber() {
        return input.mapRight(ByteArrayInputStream::new)
            .map(FFmpegFrameGrabber::new, FFmpegFrameGrabber::new);
    }

    @Nullable
    protected abstract Frame grabFrame(FFmpegFrameGrabber grabber) throws IOException;

    @Nullable
    protected abstract Frame grabFrame() throws IOException;

    @Nullable
    protected abstract E getNextFrame(FFmpegFrameGrabber grabber) throws IOException;

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
        setTimestamp(timestamp);
        E frame = getNextFrame();
        if (frame == null) {
            throw new NoSuchElementException("No frame at timestamp " + timestamp);
        } else {
            return frame;
        }
    }

    protected abstract void setTimestamp(long timestamp) throws IOException;

    @Override
    public Iterator<E> iterator() {
        try {
            var iterator = new FFmpegMediaIterator();
            toClose.add(iterator);
            return iterator;
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

    private class FFmpegMediaIterator implements Iterator<E>, Closeable {

        private final FFmpegFrameGrabber grabber;
        @Nullable
        private E nextFrame;

        private FFmpegMediaIterator() throws IOException {
            grabber = createGrabber();
            grabber.start();
            nextFrame = getNextFrame(grabber);
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
                nextFrame = getNextFrame(grabber);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return frame;
        }

        @Override
        public void close() throws IOException {
            grabber.close();
        }
    }
}
