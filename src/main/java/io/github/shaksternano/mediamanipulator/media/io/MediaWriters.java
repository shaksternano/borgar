package io.github.shaksternano.mediamanipulator.media.io;

import io.github.shaksternano.mediamanipulator.media.io.writer.FFmpegVideoWriter;
import io.github.shaksternano.mediamanipulator.media.io.writer.JavaxImageWriter;
import io.github.shaksternano.mediamanipulator.media.io.writer.MediaWriter;
import io.github.shaksternano.mediamanipulator.media.io.writer.ScrimageGifWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MediaWriters {

    private static final Map<String, MediaWriterFactory> writerFactories = new HashMap<>();

    public static MediaWriter createWriter(
        File output,
        String outputFormat,
        int audioChannels,
        int audioSampleRate,
        int audioBitrate,
        long maxFileSize,
        long maxDuration
    ) throws IOException {
        outputFormat = outputFormat.toLowerCase();
        var factory = writerFactories.getOrDefault(outputFormat, FFmpegVideoWriter::new);
        return factory.createWriter(output, outputFormat, audioChannels, audioSampleRate, audioBitrate, maxFileSize, maxDuration);
    }

    private static void registerWriterFactory(MediaWriterFactory factory, String... formats) {
        for (var format : formats) {
            writerFactories.putIfAbsent(format.toLowerCase(), factory);
        }
    }

    static {
        registerWriterFactory(
            (
                output,
                outputFormat,
                audioChannels,
                audioSampleRate,
                audioBitrate,
                maxFileSize,
                maxDuration
            ) -> new ScrimageGifWriter(output),
            "gif"
        );
        registerWriterFactory(
            (
                output,
                outputFormat,
                audioChannels,
                audioSampleRate,
                audioBitrate,
                maxFileSize,
                maxDuration
            ) -> new JavaxImageWriter(output, outputFormat),
            "bmp",
            "jpeg",
            "jpg",
            "wbmp",
            "png",
            "gif",
            "tif",
            "tiff"
        );
    }
}
