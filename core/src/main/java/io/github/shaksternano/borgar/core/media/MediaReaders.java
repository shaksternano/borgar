package io.github.shaksternano.borgar.core.media;

import io.github.shaksternano.borgar.core.exception.UnreadableFileException;
import io.github.shaksternano.borgar.core.media.readerold.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MediaReaders {

    private static final Map<String, MediaReaderFactoryOld<ImageFrameOld>> imageReaderFactories = new HashMap<>();
    private static final Map<String, MediaReaderFactoryOld<AudioFrameOld>> audioReaderFactories = new HashMap<>();

    public static MediaReader<ImageFrameOld> createImageReader(File media, String format) throws UnreadableFileException {
        var factory = getImageReaderFactory(format);
        return createReader(factory, media, format);
    }

    public static MediaReader<ImageFrameOld> createImageReader(InputStream media, String format) throws UnreadableFileException {
        var factory = getImageReaderFactory(format);
        return createReader(factory, media, format);
    }

    private static MediaReaderFactoryOld<ImageFrameOld> getImageReaderFactory(String format) {
        return imageReaderFactories.getOrDefault(format.toLowerCase(), FFmpegImageReader.Factory.INSTANCE);
    }

    public static MediaReader<AudioFrameOld> createAudioReader(File media, String format) throws UnreadableFileException {
        var factory = getAudioReaderFactory(format);
        return createReader(factory, media, format);
    }

    @SuppressWarnings("unused")
    public static MediaReader<AudioFrameOld> createAudioReader(InputStream media, String format) throws UnreadableFileException {
        var factory = getAudioReaderFactory(format);
        return createReader(factory, media, format);
    }

    private static MediaReaderFactoryOld<AudioFrameOld> getAudioReaderFactory(String format) {
        return audioReaderFactories.getOrDefault(format.toLowerCase(), FFmpegAudioReader.Factory.INSTANCE);
    }

    private static <T> MediaReader<T> createReader(MediaReaderFactoryOld<T> factory, File media, String format) throws UnreadableFileException {
        try {
            return factory.createReader(media, format);
        } catch (IOException e) {
            throw new UnreadableFileException(e);
        }
    }

    private static <T> MediaReader<T> createReader(MediaReaderFactoryOld<T> factory, InputStream media, String format) throws UnreadableFileException {
        try {
            return factory.createReader(media, format);
        } catch (IOException e) {
            throw new UnreadableFileException(e);
        }
    }

    private static void registerImageReaderFactory(MediaReaderFactoryOld<ImageFrameOld> factory, String... formats) {
        for (var format : formats) {
            imageReaderFactories.putIfAbsent(format.toLowerCase(), factory);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void registerAudioReaderFactory(MediaReaderFactoryOld<AudioFrameOld> factory, String... formats) {
        for (var format : formats) {
            audioReaderFactories.putIfAbsent(format.toLowerCase(), factory);
        }
    }

    private static void registerImageOnlyReaderFactory(MediaReaderFactoryOld<ImageFrameOld> factory, String... formats) {
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
        registerImageOnlyReaderFactory(WebPImageReader.Factory.INSTANCE,
            "webp"
        );
    }
}
