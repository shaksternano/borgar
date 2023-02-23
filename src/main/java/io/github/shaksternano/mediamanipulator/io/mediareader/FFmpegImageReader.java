package io.github.shaksternano.mediamanipulator.io.mediareader;

import io.github.shaksternano.mediamanipulator.io.MediaReaderFactory;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FFmpegImageReader extends FFmpegMediaReader<BufferedImage> {

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
    public BufferedImage getNextFrame() throws IOException {
        return converter.convert(grabber.grabImage());
    }

    @Override
    public void close() throws IOException {
        super.close();
        converter.close();
    }

    public enum Factory implements MediaReaderFactory<BufferedImage> {

        INSTANCE;

        @Override
        public MediaReader<BufferedImage> createReader(File media) throws IOException {
            return new FFmpegImageReader(media);
        }

        @Override
        public MediaReader<BufferedImage> createReader(InputStream media) throws IOException {
            return new FFmpegImageReader(media);
        }
    }
}
