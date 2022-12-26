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

    public static MediaWriter createWriter(File output, String outputFormat, double fps, int audioChannels) throws IOException {
        MediaWriterFactory factory = writerFactories.getOrDefault(outputFormat, MediaWriters::createDefaultWriter);
        return factory.createWriter(output, outputFormat, fps, audioChannels);
    }

    private static MediaWriter createDefaultWriter(File output, String outputFormat, double fps, int audioChannels) {
        return new FFmpegVideoWriter(output, outputFormat, fps, audioChannels);
    }

    private static void registerWriterFactory(MediaWriterFactory factory, String... formats) {
        for (String format : formats) {
            writerFactories.put(format, factory);
        }
    }

    static {
        registerWriterFactory(
                (output, outputFormat, fps, audioChannels) -> new ScrimageGifWriter(output, fps),
                "gif"
        );
        registerWriterFactory(
                (output, outputFormat, fps, audioChannels) -> new JavaxImageWriter(output, outputFormat),
                "bmp",
                "jpeg",
                "jpg",
                "wbmp",
                "png",
                "tif",
                "tiff"
        );
    }

    private interface MediaWriterFactory {

        MediaWriter createWriter(File output, String outputFormat, double fps, int audioChannels) throws IOException;
    }
}
