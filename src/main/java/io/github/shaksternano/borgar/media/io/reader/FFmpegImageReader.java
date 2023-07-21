package io.github.shaksternano.borgar.media.io.reader;

import com.google.common.collect.Lists;
import io.github.shaksternano.borgar.media.FrameInfo;
import io.github.shaksternano.borgar.media.ImageFrame;
import io.github.shaksternano.borgar.media.io.MediaReaderFactory;
import io.github.shaksternano.borgar.util.collect.ClosableIterator;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class FFmpegImageReader extends FFmpegMediaReader<ImageFrame> {

    private final Java2DFrameConverter converter = new Java2DFrameConverter();
    @Nullable
    private MediaReader<ImageFrame> reversed;

    public FFmpegImageReader(File input, String format) throws IOException {
        super(input, format);
    }

    public FFmpegImageReader(InputStream input, String format) throws IOException {
        super(input, format);
        toClose.add(converter);
    }

    @Override
    public MediaReader<ImageFrame> reversed() throws IOException {
        if (reversed == null) {
            reversed = new Reversed();
        }
        return reversed;
    }

    @Override
    protected void setTimestamp(long timestamp, FFmpegFrameGrabber grabber) throws IOException {
        grabber.setVideoTimestamp(timestamp);
    }

    @Nullable
    @Override
    protected Frame grabFrame(FFmpegFrameGrabber grabber) throws IOException {
        return grabber.grabImage();
    }

    @Override
    protected ImageFrame convertFrame(Frame frame) {
        if (isInvalidImageChannels(frame.imageChannels)) {
            frame.imageChannels = 3;
        }
        return new ImageFrame(
            converter.convert(frame),
            frameDuration(),
            frame.timestamp
        );
    }

    private static boolean isInvalidImageChannels(int imageChannels) {
        return imageChannels != 1 && imageChannels != 3 && imageChannels != 4;
    }

    public enum Factory implements MediaReaderFactory<ImageFrame> {

        INSTANCE;

        @Override
        public MediaReader<ImageFrame> createReader(File media, String format) throws IOException {
            return new FFmpegImageReader(media, format);
        }

        @Override
        public MediaReader<ImageFrame> createReader(InputStream media, String format) throws IOException {
            return new FFmpegImageReader(media, format);
        }
    }

    private class Reversed extends BaseMediaReader<ImageFrame> {

        private final List<FrameInfo> reversedFrameInfo;

        private Reversed() throws IOException {
            super(FFmpegImageReader.this.format());
            frameRate = FFmpegImageReader.this.frameRate;
            frameCount = FFmpegImageReader.this.frameCount;
            duration = FFmpegImageReader.this.duration;
            frameDuration = FFmpegImageReader.this.frameDuration;
            audioChannels = FFmpegImageReader.this.audioChannels;
            width = FFmpegImageReader.this.width;
            height = FFmpegImageReader.this.height;
            reversedFrameInfo = reversedFrameInfo();
        }

        private List<FrameInfo> reversedFrameInfo() throws IOException {
            List<FrameInfo> frameInfo = new ArrayList<>(frameCount);
            try (var iterator = FFmpegImageReader.this.iterator()) {
                while (iterator.hasNext()) {
                    var frame = iterator.next();
                    frameInfo.add(new FrameInfo(frame.duration(), frame.timestamp()));
                }
            }
            return Collections.unmodifiableList(Lists.reverse(frameInfo));
        }

        @Override
        public ImageFrame readFrame(long timestamp) throws IOException {
            var reversedTimestamp = duration - (timestamp % duration);
            var frame = FFmpegImageReader.this.readFrame(reversedTimestamp);
            return new ImageFrame(frame.content(), frame.duration(), timestamp);
        }

        @Override
        public MediaReader<ImageFrame> reversed() {
            return FFmpegImageReader.this;
        }

        @Override
        public void close() throws IOException {
            FFmpegImageReader.this.close();
        }

        @NotNull
        @Override
        public ClosableIterator<ImageFrame> iterator() {
            try {
                return new ReversedIterator();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private class ReversedIterator implements ClosableIterator<ImageFrame> {

            private final FFmpegFrameGrabber grabber = createGrabber();
            private final Iterator<FrameInfo> frameInfoIterator = reversedFrameInfo.iterator();
            private boolean closed = false;

            public ReversedIterator() throws IOException {
                grabber.start();
            }

            @Override
            public boolean hasNext() {
                return frameInfoIterator.hasNext();
            }

            @Override
            public ImageFrame next() {
                var info = frameInfoIterator.next();
                try {
                    return FFmpegImageReader.this.frameAtTime(info.timestamp(), grabber);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
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
}
