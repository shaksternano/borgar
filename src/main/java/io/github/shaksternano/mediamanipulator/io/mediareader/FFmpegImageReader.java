package io.github.shaksternano.mediamanipulator.io.mediareader;

import io.github.shaksternano.mediamanipulator.image.ImageFrame;
import io.github.shaksternano.mediamanipulator.io.MediaReaderFactory;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FFmpegImageReader extends FFmpegMediaReader<ImageFrame> {

    private final Java2DFrameConverter converter = new Java2DFrameConverter();

    public FFmpegImageReader(File input) throws IOException {
        super(input);
    }

    public FFmpegImageReader(InputStream input) throws IOException {
        super(input);
    }

    @Nullable
    @Override
    protected Frame grabFrame() throws IOException {
        return grabber.grabImage();
    }

    @Nullable
    @Override
    public ImageFrame getNextFrame() throws IOException {
        Frame frame = grabber.grabImage();
        if (frame == null) {
            return null;
        } else {
            return new ImageFrame(converter.convert(frame), (long) getFrameDuration(), frame.timestamp);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        converter.close();
    }

    public enum Factory implements MediaReaderFactory<ImageFrame> {

        INSTANCE;

        @Override
        public MediaReader<ImageFrame> createReader(File media) throws IOException {
            return new FFmpegImageReader(media);
        }

        @Override
        public MediaReader<ImageFrame> createReader(InputStream media) throws IOException {
            return new FFmpegImageReader(media);
        }
    }
}
