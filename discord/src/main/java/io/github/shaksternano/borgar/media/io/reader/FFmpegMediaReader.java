package io.github.shaksternano.borgar.media.io.reader;

import io.github.shaksternano.borgar.io.FileUtil;
import io.github.shaksternano.borgar.media.VideoFrame;
import io.github.shaksternano.borgar.util.MiscUtil;
import io.github.shaksternano.borgar.util.collect.ClosableIterator;
import org.apache.commons.io.FileUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public abstract sealed class FFmpegMediaReader<E extends VideoFrame<?, E>> extends BaseMediaReader<E> permits FFmpegImageReader, FFmpegAudioReader {

    private final File input;
    protected final FFmpegFrameGrabber grabber;
    protected final List<AutoCloseable> toClose = new ArrayList<>();
    private final boolean deleteFile;
    private boolean closed = false;

    public FFmpegMediaReader(File input, String format) throws IOException {
        this(input, format, false);
    }

    public FFmpegMediaReader(InputStream input, String format) throws IOException {
        this(writeToFile(input, format), format, true);
    }

    private FFmpegMediaReader(File input, String format, boolean deleteFile) throws IOException {
        super(format);
        this.input = input;
        this.deleteFile = deleteFile;
        this.grabber = createGrabber();
        toClose.add(grabber);
        grabber.start();
        int frameCount = 0;
        Frame frame;
        while ((frame = grabFrame(grabber)) != null) {
            frameCount++;
            frame.close();
        }
        frameRate = grabber.getFrameRate();
        this.frameCount = frameCount;
        duration = grabber.getTimestamp();
        frameDuration = 1_000_000 / frameRate;
        audioChannels = grabber.getAudioChannels();
        audioSampleRate = grabber.getSampleRate();
        audioBitrate = grabber.getAudioBitrate();
        width = grabber.getImageWidth();
        height = grabber.getImageHeight();
    }

    private static File writeToFile(InputStream input, String format) throws IOException {
        var file = FileUtil.createTempFile("video", format);
        FileUtils.copyInputStreamToFile(input, file);
        return file;
    }

    protected FFmpegFrameGrabber createGrabber() {
        return new FFmpegFrameGrabber(input);
    }

    @Override
    public E readFrame(long timestamp) throws IOException {
        long circularTimestamp = timestamp % Math.max(duration(), 1);
        return frameNonCircular(circularTimestamp, grabber);
    }

    protected E frameAtTime(long timestamp, FFmpegFrameGrabber grabber) throws IOException {
        long circularTimestamp = timestamp % Math.max(duration() + 1, 1);
        return frameNonCircular(circularTimestamp, grabber);
    }

    @Override
    public E first() throws IOException {
        return frameNonCircular(0, grabber);
    }

    private E frameNonCircular(long timestamp, FFmpegFrameGrabber grabber) throws IOException {
        var frame = findFrame(timestamp, grabber);
        return convertFrame(frame);
    }

    private Frame findFrame(long timestamp, FFmpegFrameGrabber grabber) throws IOException {
        setTimestamp(timestamp, grabber);
        var frame = grabFrame(grabber);
        if (frame == null) {
            throw new NoSuchElementException("No frame at timestamp " + timestamp);
        }
        // The next call to grabFrame() will overwrite the current frame object, so we need to clone it
        var correctFrame = frame.clone();
        // The frame grabbed might not have the exact timestamp, even if there is a frame with that timestamp.
        // We keep grabbing frames, until we find one with a timestamp greater or equal to the requested timestamp.
        while (correctFrame.timestamp < timestamp) {
            var newFrame = grabFrame(grabber);
            if (newFrame == null || newFrame.timestamp > timestamp) {
                return correctFrame;
            }
            correctFrame.close();
            var copy = newFrame.clone();
            newFrame.close();
            correctFrame = copy;
        }
        return correctFrame;
    }

    protected abstract void setTimestamp(long timestamp, FFmpegFrameGrabber grabber) throws IOException;

    @Nullable
    protected abstract Frame grabFrame(FFmpegFrameGrabber grabber) throws IOException;

    @Nullable
    protected E grabConvertedFrame(FFmpegFrameGrabber grabber) throws IOException {
        var frame = grabFrame(grabber);
        if (frame == null) {
            return null;
        }
        return convertFrame(frame);
    }

    protected abstract E convertFrame(Frame frame);

    @Override
    public ClosableIterator<E> iterator() {
        try {
            return new Iterator();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        MiscUtil.closeAll(toClose);
        if (deleteFile) {
            input.delete();
        }
    }

    private class Iterator implements ClosableIterator<E> {

        private final FFmpegFrameGrabber grabber;
        @Nullable
        private E nextFrame;
        private boolean closed = false;

        private Iterator() throws IOException {
            grabber = createGrabber();
            grabber.start();
            nextFrame = grabConvertedFrame(grabber);
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
                nextFrame = grabConvertedFrame(grabber);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return frame;
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                grabber.close();
            }
        }
    }
}
