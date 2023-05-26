package io.github.shaksternano.mediamanipulator.media.io;

import io.github.shaksternano.mediamanipulator.media.io.writer.MediaWriter;

import java.io.File;
import java.io.IOException;

@FunctionalInterface
public interface MediaWriterFactory {

    MediaWriter createWriter(File output, String outputFormat, int audioChannels) throws IOException;
}
