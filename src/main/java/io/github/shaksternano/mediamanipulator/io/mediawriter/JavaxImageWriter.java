package io.github.shaksternano.mediamanipulator.io.mediawriter;

import io.github.shaksternano.mediamanipulator.image.ImageFrame;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class JavaxImageWriter extends NoAudioWriter {

    private final File output;
    private final String outputFormat;
    private boolean written = false;

    public JavaxImageWriter(File output, String outputFormat) {
        this.output = output;
        this.outputFormat = outputFormat;
    }

    @Override
    public void recordImageFrame(ImageFrame frame) throws IOException {
        if (!written) {
            ImageIO.write(frame.content(), outputFormat, output);
            written = true;
        }
    }

    @Override
    public void close() {
    }
}
