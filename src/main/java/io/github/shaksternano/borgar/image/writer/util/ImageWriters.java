package io.github.shaksternano.borgar.image.writer.util;

import io.github.shaksternano.borgar.Main;
import io.github.shaksternano.borgar.image.imagemedia.ImageMedia;
import io.github.shaksternano.borgar.image.writer.Image4jIcoImageWriter;
import io.github.shaksternano.borgar.image.writer.ImageWriter;
import io.github.shaksternano.borgar.image.writer.JavaxImageWriter;
import io.github.shaksternano.borgar.image.writer.ScrimageAnimatedGifWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageWriters {

    public static void registerImageWriters() {
        ImageWriterRegistry.register(new ScrimageAnimatedGifWriter());
        ImageWriterRegistry.register(new JavaxImageWriter());
        ImageWriterRegistry.register(new Image4jIcoImageWriter());
    }

    public static void write(ImageMedia image, File file, String format) throws IOException {
        if (image.isEmpty()) {
            throw new IllegalArgumentException("ImageMedia is empty!");
        } else {
            List<ImageWriter> writers = ImageWriterRegistry.getWriters(format);
            if (writers.isEmpty()) {
                throw new IOException("No image writers found for format: " + format + "!");
            } else {
                for (ImageWriter writer : writers) {
                    try {
                        writer.write(image, file, format);
                        return;
                    } catch (IOException e) {
                        Main.getLogger().error("Error while writing image with format " + format + "!", e);
                    }
                }

                throw new IOException("Could not write image with format " + format + "!");
            }
        }
    }
}
