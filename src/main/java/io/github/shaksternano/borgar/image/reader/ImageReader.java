package io.github.shaksternano.borgar.image.reader;

import io.github.shaksternano.borgar.image.imagemedia.ImageMedia;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface ImageReader {

    ImageMedia read(InputStream inputStream, @Nullable Integer type) throws IOException;

    ImageMedia read(File file, @Nullable Integer type) throws IOException;

    Set<String> getSupportedFormats();
}
