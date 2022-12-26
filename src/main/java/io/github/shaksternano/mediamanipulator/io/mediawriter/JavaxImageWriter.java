package io.github.shaksternano.mediamanipulator.io.mediawriter;

import org.bytedeco.javacv.Frame;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class JavaxImageWriter implements MediaWriter {

    private final File output;
    private final String outputFormat;
    private boolean written = false;

    public JavaxImageWriter(File output, String outputFormat) {
        this.output = output;
        this.outputFormat = outputFormat;
    }

    @Override
    public void recordImageFrame(BufferedImage frame) throws IOException {
        if (!written) {
            ImageIO.write(frame, outputFormat, output);
            written = true;
        }
    }

    @Override
    public void recordAudioFrame(Frame frame) {
    }

    @Override
    public void close() {
    }
}
