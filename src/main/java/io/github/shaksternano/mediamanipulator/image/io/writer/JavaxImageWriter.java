package io.github.shaksternano.mediamanipulator.image.io.writer;

import com.google.common.collect.ImmutableSet;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public class JavaxImageWriter implements ImageWriter {

    @Override
    public void write(ImageMedia image, OutputStream outputStream, String format) throws IOException {
        BufferedImage bufferedImage = image.getFrame(0).getImage();
        ImageIO.write(bufferedImage, format, outputStream);
    }

    @Override
    public Set<String> getSupportedFormats() {
        return ImmutableSet.of(
                "bmp",
                "jpeg",
                "jpg",
                "wbmp",
                "png",
                "gif",
                "tif",
                "tiff"
        );
    }
}
