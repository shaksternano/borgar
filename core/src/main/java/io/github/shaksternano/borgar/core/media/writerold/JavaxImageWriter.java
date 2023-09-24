package io.github.shaksternano.borgar.core.media.writerold;

import io.github.shaksternano.borgar.core.media.ImageFrameOld;
import io.github.shaksternano.borgar.core.media.ImageUtil;
import io.github.shaksternano.borgar.core.media.MediaUtil;

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
    public void writeImageFrame(ImageFrameOld frame) throws IOException {
        if (!written) {
            written = true;
            BufferedImage image;
            if (MediaUtil.supportsTransparency(outputFormat)) {
                image = ImageUtil.convertType(frame.getContent(), BufferedImage.TYPE_INT_ARGB);
            } else {
                image = ImageUtil.convertType(frame.getContent(), BufferedImage.TYPE_3BYTE_BGR);
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
