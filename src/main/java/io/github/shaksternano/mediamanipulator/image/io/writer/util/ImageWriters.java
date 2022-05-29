package io.github.shaksternano.mediamanipulator.image.io.writer.util;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.io.writer.ImageWriter;
import io.github.shaksternano.mediamanipulator.image.io.writer.JavaxImageWriter;
import io.github.shaksternano.mediamanipulator.image.io.writer.ScrimageAnimatedGifWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ImageWriters {

    public static void registerImageWriters() {
        ImageWriterRegistry.register(new ScrimageAnimatedGifWriter());
        ImageWriterRegistry.register(new JavaxImageWriter());
    }

    public static void write(ImageMedia image, File file, String format) throws IOException {
        try (OutputStream fileOutputStream = new FileOutputStream(file)) {
            write(image, fileOutputStream, format);
        }
    }

    public static void write(ImageMedia image, OutputStream outputStream, String format) throws IOException {
        List<ImageWriter> writers = ImageWriterRegistry.getWriters(format);
        if (writers.isEmpty()) {
            throw new IOException("No image writers found for format: " + format + "!");
        } else {
            for (ImageWriter writer : writers) {
                try {
                    writer.write(image, outputStream, format);
                    return;
                } catch (IOException e) {
                    Main.getLogger().error("Error while writing image with format " + format + "!", e);
                }
            }

            throw new IOException("Could not write image with format " + format + "!");
        }
    }
}
