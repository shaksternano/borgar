package io.github.shaksternano.mediamanipulator.io;

import io.github.shaksternano.mediamanipulator.image.ImageFrame;
import io.github.shaksternano.mediamanipulator.io.mediareader.*;
import org.bytedeco.javacv.Frame;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MediaReaders {

    private static final Map<String, MediaReaderFactory<ImageFrame>> imageReaderFactories = new HashMap<>();
    private static final Map<String, MediaReaderFactory<Frame>> audioReaderFactories = new HashMap<>();

    public static MediaReader<ImageFrame> createImageReader(File media, String format) throws IOException {
        MediaReaderFactory<ImageFrame> factory = getImageReaderFactory(format);
        return factory.createReader(media);
    }

    public static MediaReader<ImageFrame> createImageReader(InputStream media, String format) throws IOException {
        MediaReaderFactory<ImageFrame> factory = getImageReaderFactory(format);
        return factory.createReader(media);
    }

    private static MediaReaderFactory<ImageFrame> getImageReaderFactory(String format) {
        return imageReaderFactories.getOrDefault(format, FFmpegImageReader.Factory.INSTANCE);
    }

    public static MediaReader<Frame> createAudioReader(File media, String format) throws IOException {
        MediaReaderFactory<Frame> factory = getAudioReaderFactory(format);
        return factory.createReader(media);
    }

    @SuppressWarnings("unused")
    public static MediaReader<Frame> createAudioReader(InputStream media, String format) throws IOException {
        MediaReaderFactory<Frame> factory = getAudioReaderFactory(format);
        return factory.createReader(media);
    }

    private static MediaReaderFactory<Frame> getAudioReaderFactory(String format) {
        return audioReaderFactories.getOrDefault(format, FFmpegAudioReader.Factory.INSTANCE);
    }

    private static void registerImageReaderFactory(MediaReaderFactory<ImageFrame> factory, String... formats) {
        for (String format : formats) {
            imageReaderFactories.put(format, factory);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void registerAudioReaderFactory(MediaReaderFactory<Frame> factory, String... formats) {
        for (String format : formats) {
            audioReaderFactories.put(format, factory);
        }
    }

    private static void registerImageOnlyReaderFactory(MediaReaderFactory<ImageFrame> factory, String... formats) {
        registerImageReaderFactory(factory, formats);
        registerAudioReaderFactory(NoAudioReader.Factory.INSTANCE, formats);
    }

    static {
        registerImageOnlyReaderFactory(ScrimageGifReader.Factory.INSTANCE, "gif");
        registerImageOnlyReaderFactory(JavaxImageReader.Factory.INSTANCE,
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
