package io.github.shaksternano.mediamanipulator.image.writer;

import com.google.common.collect.ImmutableSet;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.util.MediaCompression;
import net.ifok.image.image4j.codec.ico.ICOEncoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class Image4jIcoImageWriter implements ImageWriter {

    @Override
    public void write(ImageMedia image, File file, String format) throws IOException {
        BufferedImage original = image.getFirstImage();
        BufferedImage reduced = MediaCompression.reduceToSize(original, 256, 256);
        ICOEncoder.write(reduced, file);
    }

    @Override
    public Set<String> getSupportedFormats() {
        return ImmutableSet.of(
            "ico"
        );
    }
}
