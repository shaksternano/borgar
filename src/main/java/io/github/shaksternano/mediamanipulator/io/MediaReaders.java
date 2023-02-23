package io.github.shaksternano.mediamanipulator.io;

import io.github.shaksternano.mediamanipulator.io.mediareader.*;
import org.bytedeco.javacv.Frame;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MediaReaders {

    private static final Map<String, MediaReaderFactory<BufferedImage>> imageReaderFactories = new HashMap<>();
    private static final Map<String, MediaReaderFactory<Frame>> audioReaderFactories = new HashMap<>();

    public static MediaReader<BufferedImage> createImageReader(File media, String format) throws IOException {
        MediaReaderFactory<BufferedImage> factory = imageReaderFactories.getOrDefault(format, MediaReaders::createDefaultImageReader);
        return factory.createReader(media);
    }

    public static MediaReader<Frame> createAudioReader(File media, String format) throws IOException {
        MediaReaderFactory<Frame> factory = audioReaderFactories.getOrDefault(format, MediaReaders::createDefaultAudioReader);
        return factory.createReader(media);
    }

    private static void registerImageReaderFactory(MediaReaderFactory<BufferedImage> factory, String... formats) {
        for (String format : formats) {
            imageReaderFactories.put(format, factory);
        }
    }

    private static void registerAudioReaderFactory(MediaReaderFactory<Frame> factory, String... formats) {
        for (String format : formats) {
            audioReaderFactories.put(format, factory);
        }
    }

    private static void registerImageOnlyReaderFactory(MediaReaderFactory<BufferedImage> factory, String... formats) {
        registerImageReaderFactory(factory, formats);
        registerAudioReaderFactory(media -> NoAudioReader.INSTANCE, formats);
    }

    private static MediaReader<BufferedImage> createDefaultImageReader(File media) throws IOException {
        return new FFmpegImageReader(media);
    }

    private static MediaReader<Frame> createDefaultAudioReader(File media) throws IOException {
        return new FFmpegAudioReader(media);
    }

    static {
        registerImageOnlyReaderFactory(ScrimageGifReader::new, "gif");
    }

    @FunctionalInterface
    private interface MediaReaderFactory<T> {
        MediaReader<T> createReader(File media) throws IOException;
    }
}
