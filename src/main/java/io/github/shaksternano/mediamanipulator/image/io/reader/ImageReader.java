package io.github.shaksternano.mediamanipulator.image.io.reader;

import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface ImageReader {

    ImageMedia read(File file, @Nullable Integer type) throws IOException;

    Set<String> getSupportedFormats();
}
