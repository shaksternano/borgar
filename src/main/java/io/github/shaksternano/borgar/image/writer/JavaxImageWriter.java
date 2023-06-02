package io.github.shaksternano.borgar.image.writer;

import com.google.common.collect.ImmutableSet;
import io.github.shaksternano.borgar.image.imagemedia.ImageMedia;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class JavaxImageWriter implements ImageWriter {

    @Override
    public void write(ImageMedia image, File file, String format) throws IOException {
        BufferedImage bufferedImage = image.getFirstImage();
        ImageIO.write(bufferedImage, format, file);
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
