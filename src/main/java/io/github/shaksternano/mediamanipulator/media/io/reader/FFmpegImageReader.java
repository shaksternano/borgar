package io.github.shaksternano.mediamanipulator.media.io.reader;

import io.github.shaksternano.mediamanipulator.media.ImageFrame;
import io.github.shaksternano.mediamanipulator.media.io.MediaReaderFactory;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FFmpegImageReader extends FFmpegMediaReader<ImageFrame> {

    private final Java2DFrameConverter converter = new Java2DFrameConverter();

    public FFmpegImageReader(File input, String format) throws IOException {
        super(input, format);
    }

    public FFmpegImageReader(InputStream input, String format) throws IOException {
        super(input, format);
    }

    @Nullable
    @Override
    protected Frame grabFrame() throws IOException {
        return grabber.grabImage();
    }

    @Nullable
    @Override
    protected ImageFrame getNextFrame() throws IOException {
        var frame = grabFrame();
        if (frame == null) {
            return null;
        } else {
            return new ImageFrame(converter.convert(frame), frameDuration(), frame.timestamp);
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        super.close();
        converter.close();
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
}
