package io.github.shaksternano.borgar.core.media.readerold;

import com.google.common.collect.Lists;
import io.github.shaksternano.borgar.core.collect.ClosableIteratorOld;
import io.github.shaksternano.borgar.core.media.FrameInfo;
import io.github.shaksternano.borgar.core.media.ImageFrameOld;
import io.github.shaksternano.borgar.core.media.MediaReaderFactoryOld;
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

public final class FFmpegImageReader extends FFmpegMediaReader<ImageFrameOld> {

    private final Java2DFrameConverter converter = new Java2DFrameConverter();
    @Nullable
    private MediaReader<ImageFrameOld> reversed;

    public FFmpegImageReader(File input, String format) throws IOException {
        super(input, format);
    }

    public FFmpegImageReader(InputStream input, String format) throws IOException {
        super(input, format);
        toClose.add(converter);
    }

    @Override
    public MediaReader<ImageFrameOld> reversed() throws IOException {
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
    protected ImageFrameOld convertFrame(Frame frame) {
        if (isInvalidImageChannels(frame.imageChannels)) {
            frame.imageChannels = 3;
        }
        return new ImageFrameOld(
            converter.convert(frame),
            frameDuration(),
            frame.timestamp
        );
    }

    private static boolean isInvalidImageChannels(int imageChannels) {
        return imageChannels != 1 && imageChannels != 3 && imageChannels != 4;
    }

    public enum Factory implements MediaReaderFactoryOld<ImageFrameOld> {

        INSTANCE;

        @Override
        public MediaReader<ImageFrameOld> createReader(File media, String format) throws IOException {
            return new FFmpegImageReader(media, format);
        }

        @Override
        public MediaReader<ImageFrameOld> createReader(InputStream media, String format) throws IOException {
            return new FFmpegImageReader(media, format);
        }
    }

    private class Reversed extends BaseMediaReader<ImageFrameOld> {

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
                    frameInfo.add(new FrameInfo(frame.getDuration(), frame.getTimestamp()));
                }
            }
            return Collections.unmodifiableList(Lists.reverse(frameInfo));
        }

        @Override
        public ImageFrameOld readFrame(long timestamp) throws IOException {
            var reversedTimestamp = duration - (timestamp % duration);
            var frame = FFmpegImageReader.this.readFrame(reversedTimestamp);
            return new ImageFrameOld(frame.getContent(), frame.getDuration(), timestamp);
        }

        @Override
        public MediaReader<ImageFrameOld> reversed() {
            return FFmpegImageReader.this;
        }

        @Override
        public void close() throws IOException {
            FFmpegImageReader.this.close();
        }

        @NotNull
        @Override
        public ClosableIteratorOld<ImageFrameOld> iterator() {
            try {
                return new ReversedIterator();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private class ReversedIterator implements ClosableIteratorOld<ImageFrameOld> {

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
            public ImageFrameOld next() {
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
