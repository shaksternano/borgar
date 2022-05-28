package io.github.shaksternano.mediamanipulator.image.io.writer;

import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public interface ImageWriter {

    void write(ImageMedia image, OutputStream outputStream, String format) throws IOException;

    Set<String> getSupportedFormats();
}
