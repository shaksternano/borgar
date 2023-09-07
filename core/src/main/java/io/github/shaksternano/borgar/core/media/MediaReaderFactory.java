package io.github.shaksternano.borgar.core.media;

import io.github.shaksternano.borgar.core.media.reader.MediaReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface MediaReaderFactory<T> {

    MediaReader<T> createReader(File media, String format) throws IOException;

    MediaReader<T> createReader(InputStream media, String format) throws IOException;
}
