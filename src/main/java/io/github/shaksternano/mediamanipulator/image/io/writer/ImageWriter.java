package io.github.shaksternano.mediamanipulator.image.io.writer;

import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface ImageWriter {

    void write(ImageMedia image, File file, String format) throws IOException;

    Set<String> getSupportedFormats();
}
