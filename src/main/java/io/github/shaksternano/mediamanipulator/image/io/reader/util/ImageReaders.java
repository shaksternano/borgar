package io.github.shaksternano.mediamanipulator.image.io.reader.util;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.exception.UnreadableFileException;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.io.reader.ImageReader;
import io.github.shaksternano.mediamanipulator.image.io.reader.JavaxImageReader;
import io.github.shaksternano.mediamanipulator.image.io.reader.ScrimageAnimatedGifReader;
import io.github.shaksternano.mediamanipulator.image.io.reader.ScrimageImageReader;
import io.github.shaksternano.mediamanipulator.image.io.writer.Image4jIcoImageWriter;
import io.github.shaksternano.mediamanipulator.image.io.writer.util.ImageWriterRegistry;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageReaders {

    public static void registerImageReaders() {
        ImageReaderRegistry.register(new ScrimageAnimatedGifReader());
        ImageReaderRegistry.register(new ScrimageImageReader());
        ImageReaderRegistry.register(new JavaxImageReader());
        ImageWriterRegistry.register(new Image4jIcoImageWriter());
    }

    public static ImageMedia read(File file, String imageFormat, @Nullable Integer imageType) throws UnreadableFileException {
        List<ImageReader> readers = ImageReaderRegistry.getReaders(imageFormat);
        if (readers.isEmpty()) {
            throw new UnreadableFileException("No image reader found for image type " + imageFormat + "!");
        } else {
            for (ImageReader reader : readers) {
                try {
                    return reader.read(file, imageType);
                } catch (IOException e) {
                    Main.getLogger().error("Error reading image with reader " + reader.getClass().getSimpleName() + "!", e);
                }
            }

            throw new UnreadableFileException("Could not read image with type " + imageFormat + "!");
        }
    }
}
