package io.github.shaksternano.mediamanipulator.io;

import io.github.shaksternano.mediamanipulator.io.mediawriter.FFmpegVideoWriter;
import io.github.shaksternano.mediamanipulator.io.mediawriter.JavaxImageWriter;
import io.github.shaksternano.mediamanipulator.io.mediawriter.MediaWriter;
import io.github.shaksternano.mediamanipulator.io.mediawriter.ScrimageGifWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MediaWriters {

    private static final Map<String, MediaWriterFactory> writerFactories = new HashMap<>();

    public static MediaWriter createWriter(File output, String outputFormat, int audioChannels) throws IOException {
        outputFormat = outputFormat.toLowerCase();
        MediaWriterFactory factory = writerFactories.getOrDefault(outputFormat, FFmpegVideoWriter::new);
        return factory.createWriter(output, outputFormat, audioChannels);
    }

    private static void registerWriterFactory(MediaWriterFactory factory, String... formats) {
        for (String format : formats) {
            writerFactories.put(format.toLowerCase(), factory);
        }
    }

    static {
        registerWriterFactory(
            (output, outputFormat, audioChannels) -> new ScrimageGifWriter(output),
            "gif"
        );
        registerWriterFactory(
            (output, outputFormat, audioChannels) -> new JavaxImageWriter(output, outputFormat),
            "bmp",
            "jpeg",
            "jpg",
            "wbmp",
            "png",
            "tif",
            "tiff"
        );
    }
}
