package io.github.shaksternano.mediamanipulator.media.io.writer;

import io.github.shaksternano.mediamanipulator.media.ImageFrame;

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
    public void writeImageFrame(ImageFrame frame) throws IOException {
        if (!written) {
            ImageIO.write(frame.content(), outputFormat, output);
            written = true;
        }
    }

    @Override
    public void close() {
    }
}
