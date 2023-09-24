package io.github.shaksternano.borgar.core.media.writerold;

import com.sksamuel.scrimage.ImmutableImage;
import io.github.shaksternano.borgar.core.media.ImageFrameOld;
import net.ifok.image.image4j.codec.ico.ICOEncoder;

import java.io.File;
import java.io.IOException;

public class Image4jIcoWriter implements NoAudioWriter {

    private final File output;
    private boolean written = false;

    public Image4jIcoWriter(File output) {
        this.output = output;
    }

    @Override
    public void writeImageFrame(ImageFrameOld frame) throws IOException {
        if (!written) {
            written = true;
            var image = ImmutableImage.wrapAwt(frame.getContent())
                .bound(256, 256)
                .awt();
            ICOEncoder.write(image, output);
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
