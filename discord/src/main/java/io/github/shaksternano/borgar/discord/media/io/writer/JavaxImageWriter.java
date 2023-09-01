package io.github.shaksternano.borgar.discord.media.io.writer;

import io.github.shaksternano.borgar.discord.media.ImageFrame;
import io.github.shaksternano.borgar.discord.media.ImageUtil;
import io.github.shaksternano.borgar.discord.media.MediaUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class JavaxImageWriter implements NoAudioWriter {

    private final File output;
    private final String outputFormat;
    private boolean written = false;

    public JavaxImageWriter(File output, String outputFormat) {
        this.output = output;
        this.outputFormat = outputFormat;
    }

    @Override
    public void writeImageFrame(ImageFrame frame) throws IOException {
        if (!written) {
            written = true;
            BufferedImage image;
            if (MediaUtil.supportsTransparency(outputFormat)) {
                image = ImageUtil.convertType(frame.content(), BufferedImage.TYPE_INT_ARGB);
            } else {
                image = ImageUtil.convertType(frame.content(), BufferedImage.TYPE_3BYTE_BGR);
            }
            var supportedFormat = ImageIO.write(image, outputFormat, output);
            if (!supportedFormat) {
                throw new IOException("Unsupported image format: " + outputFormat);
            }
        }
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public void close() {
    }
}
