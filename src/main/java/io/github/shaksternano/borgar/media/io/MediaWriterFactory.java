package io.github.shaksternano.borgar.media.io;

import io.github.shaksternano.borgar.media.io.writer.MediaWriter;

import java.io.File;
import java.io.IOException;

@FunctionalInterface
public interface MediaWriterFactory {

    MediaWriter createWriter(
        File output,
        String outputFormat,
        int audioChannels,
        int audioSampleRate,
        int audioBitrate,
        long maxFileSize,
        long maxDuration
    ) throws IOException;
}
