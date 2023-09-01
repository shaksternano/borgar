package io.github.shaksternano.borgar.media.io;

import io.github.shaksternano.borgar.media.io.writer.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MediaWriters {

    private static final Map<String, MediaWriterFactory> writerFactories = new HashMap<>();

    public static MediaWriter createWriter(
        File output,
        String outputFormat,
        int loopCount,
        int audioChannels,
        int audioSampleRate,
        int audioBitrate,
        long maxFileSize,
        long maxDuration
    ) throws IOException {
        outputFormat = outputFormat.toLowerCase();
        var factory = writerFactories.getOrDefault(outputFormat, (
            output1,
            outputFormat1,
            loopCount1,
            audioChannels1,
            audioSampleRate1,
            audioBitrate1,
            maxFileSize1,
            maxDuration1
        ) -> new FFmpegVideoWriter(output1, outputFormat1, audioChannels1, audioSampleRate1, audioBitrate1, maxFileSize1, maxDuration1));
        return factory.createWriter(
            output,
            outputFormat,
            loopCount,
            audioChannels,
            audioSampleRate,
            audioBitrate,
            maxFileSize,
            maxDuration
        );
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
                loopCount,
                audioChannels,
                audioSampleRate,
                audioBitrate,
                maxFileSize,
                maxDuration
            ) -> new ScrimageGifWriter(output, loopCount),
            "gif"
        );
        registerWriterFactory(
            (
                output,
                outputFormat,
                loopCount,
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
        registerWriterFactory(
            (
                output,
                outputFormat,
                loopCount,
                audioChannels,
                audioSampleRate,
                audioBitrate,
                maxFileSize,
                maxDuration
            ) -> new Image4jIcoWriter(output),
            "ico"
        );
    }
}
