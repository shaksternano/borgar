package io.github.shaksternano.borgar.image.reader.util;

import io.github.shaksternano.borgar.Main;
import io.github.shaksternano.borgar.exception.UnreadableFileException;
import io.github.shaksternano.borgar.image.imagemedia.ImageMedia;
import io.github.shaksternano.borgar.image.reader.ImageReader;
import io.github.shaksternano.borgar.image.reader.JavaxImageReader;
import io.github.shaksternano.borgar.image.reader.ScrimageAnimatedGifReader;
import io.github.shaksternano.borgar.image.reader.ScrimageImageReader;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.List;

public class ImageReaders {

    public static void registerImageReaders() {
        ImageReaderRegistry.register(new ScrimageAnimatedGifReader());
        ImageReaderRegistry.register(new ScrimageImageReader());
        ImageReaderRegistry.register(new JavaxImageReader());
    }

    public static ImageMedia read(File file, String imageFormat, @Nullable Integer imageType) {
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

    public static ImageMedia read(InputStream inputStream, String imageFormat, @Nullable Integer imageType) throws IOException {
        List<ImageReader> readers = ImageReaderRegistry.getReaders(imageFormat);
        if (readers.isEmpty()) {
            throw new UnreadableFileException("No image reader found for image type " + imageFormat + "!");
        } else {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            inputStream.transferTo(outputStream);
            inputStream.close();
            for (ImageReader reader : readers) {
                try {
                    return reader.read(new ByteArrayInputStream(outputStream.toByteArray()), imageType);
                } catch (IOException e) {
                    Main.getLogger().error("Error reading image with reader " + reader.getClass().getSimpleName() + "!", e);
                }
            }
            outputStream.close();

            throw new UnreadableFileException("Could not read image with type " + imageFormat + "!");
        }
    }
}
