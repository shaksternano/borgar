package io.github.shaksternano.mediamanipulator.image.io.reader;

import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface ImageReader {

    ImageMedia read(InputStream inputStream, @Nullable Integer type) throws IOException;

    Set<String> getSupportedFormats();
}
