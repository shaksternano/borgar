package io.github.shaksternano.mediamanipulator.media.io;

import io.github.shaksternano.mediamanipulator.media.AudioFrame;
import io.github.shaksternano.mediamanipulator.media.ImageFrame;
import io.github.shaksternano.mediamanipulator.media.io.reader.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MediaReaders {

    private static final Map<String, MediaReaderFactory<ImageFrame>> imageReaderFactories = new HashMap<>();
    private static final Map<String, MediaReaderFactory<AudioFrame>> audioReaderFactories = new HashMap<>();

    public static MediaReader<ImageFrame> createImageReader(File media, String format) throws IOException {
        MediaReaderFactory<ImageFrame> factory = getImageReaderFactory(format);
        return factory.createReader(media, format);
    }

    public static MediaReader<ImageFrame> createImageReader(InputStream media, String format) throws IOException {
        MediaReaderFactory<ImageFrame> factory = getImageReaderFactory(format);
        return factory.createReader(media, format);
    }

    private static MediaReaderFactory<ImageFrame> getImageReaderFactory(String format) {
        return imageReaderFactories.getOrDefault(format.toLowerCase(), FFmpegImageReader.Factory.INSTANCE);
    }

    public static MediaReader<AudioFrame> createAudioReader(File media, String format) throws IOException {
        MediaReaderFactory<AudioFrame> factory = getAudioReaderFactory(format);
        return factory.createReader(media, format);
    }

    @SuppressWarnings("unused")
    public static MediaReader<AudioFrame> createAudioReader(InputStream media, String format) throws IOException {
        MediaReaderFactory<AudioFrame> factory = getAudioReaderFactory(format);
        return factory.createReader(media, format);
    }

    private static MediaReaderFactory<AudioFrame> getAudioReaderFactory(String format) {
        return audioReaderFactories.getOrDefault(format.toLowerCase(), FFmpegAudioReader.Factory.INSTANCE);
    }

    private static void registerImageReaderFactory(MediaReaderFactory<ImageFrame> factory, String... formats) {
        for (String format : formats) {
            imageReaderFactories.putIfAbsent(format.toLowerCase(), factory);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void registerAudioReaderFactory(MediaReaderFactory<AudioFrame> factory, String... formats) {
        for (String format : formats) {
            audioReaderFactories.putIfAbsent(format.toLowerCase(), factory);
        }
    }

    private static void registerImageOnlyReaderFactory(MediaReaderFactory<ImageFrame> factory, String... formats) {
        registerImageReaderFactory(factory, formats);
        registerAudioReaderFactory(NoAudioReader.Factory.INSTANCE, formats);
    }

    static {
        registerImageOnlyReaderFactory(ScrimageGifReader.Factory.INSTANCE,
            "gif"
        );
        registerImageOnlyReaderFactory(JavaxImageReader.Factory.INSTANCE,
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
