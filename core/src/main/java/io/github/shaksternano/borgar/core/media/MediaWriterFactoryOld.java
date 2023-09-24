package io.github.shaksternano.borgar.core.media;

import io.github.shaksternano.borgar.core.media.writerold.MediaWriter;

import java.io.File;
import java.io.IOException;

@FunctionalInterface
public interface MediaWriterFactoryOld {

    MediaWriter createWriter(
        File output,
        String outputFormat,
        int loopCount,
        int audioChannels,
        int audioSampleRate,
        int audioBitrate,
        long maxFileSize,
        long maxDuration
    ) throws IOException;
}
