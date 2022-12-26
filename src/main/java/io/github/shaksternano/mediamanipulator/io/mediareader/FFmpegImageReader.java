package io.github.shaksternano.mediamanipulator.io.mediareader;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FFmpegImageReader extends FFmpegMediaReader<BufferedImage> {

    private final Java2DFrameConverter converter = new Java2DFrameConverter();

    public FFmpegImageReader(File input) throws IOException {
        super(input);
    }

    @Nullable
    @Override
    protected BufferedImage getNextFrame() throws IOException {
        return converter.convert(grabber.grabImage());
    }

    @Override
    public void close() throws IOException {
        super.close();
        converter.close();
    }
}
