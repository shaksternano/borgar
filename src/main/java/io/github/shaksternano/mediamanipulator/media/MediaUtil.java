package io.github.shaksternano.mediamanipulator.media;

import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.media.io.MediaReaders;
import io.github.shaksternano.mediamanipulator.media.io.MediaWriters;
import io.github.shaksternano.mediamanipulator.media.io.imageprocessor.BasicImageProcessor;
import io.github.shaksternano.mediamanipulator.media.io.imageprocessor.DualImageProcessor;
import io.github.shaksternano.mediamanipulator.media.io.imageprocessor.SingleImageProcessor;
import io.github.shaksternano.mediamanipulator.media.io.reader.MediaReader;
import io.github.shaksternano.mediamanipulator.media.io.reader.ZippedMediaReader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class MediaUtil {

    public static File processMedia(
        File media,
        String outputFormat,
        String resultName,
        UnaryOperator<BufferedImage> imageMapper
    ) throws IOException {
        return processMedia(
            media,
            outputFormat,
            resultName,
            new BasicImageProcessor(imageMapper)
        );
    }

    public static <T> File processMedia(
        File media,
        String outputFormat,
        String resultName,
        SingleImageProcessor<T> processor
    ) throws IOException {
        var output = FileUtil.createTempFile(resultName, outputFormat);
        return processMedia(media, outputFormat, output, processor);
    }

    public static <T> File processMedia(
        File media,
        String outputFormat,
        File output,
        SingleImageProcessor<T> processor
    ) throws IOException {
        var imageReader = MediaReaders.createImageReader(media, outputFormat);
        var audioReader = MediaReaders.createAudioReader(media, outputFormat);
        return processMedia(imageReader, audioReader, output, outputFormat, processor);
    }

    public static <T> File processMedia(
        MediaReader<ImageFrame> imageReader,
        MediaReader<AudioFrame> audioReader,
        String outputFormat,
        String resultName,
        SingleImageProcessor<T> processor
    ) throws IOException {
        var output = FileUtil.createTempFile(resultName, outputFormat);
        return processMedia(imageReader, audioReader, output, outputFormat, processor);
    }

    public static <T> File processMedia(
        MediaReader<ImageFrame> imageReader,
        MediaReader<AudioFrame> audioReader,
        File output,
        String outputFormat,
        SingleImageProcessor<T> processor
    ) throws IOException {
        if (processor.speed() < 0) {
            imageReader = imageReader.reversed();
            audioReader = audioReader.reversed();
        }
        var finalImageReader = imageReader;
        var finalAudioReader = audioReader;
        try (
            finalImageReader;
            finalAudioReader;
            processor;
            var writer = MediaWriters.createWriter(
                output,
                outputFormat,
                finalAudioReader.audioChannels(),
                finalAudioReader.audioSampleRate(),
                finalAudioReader.audioBitrate());
            var imageIterator = finalImageReader.iterator();
            var audioIterator = finalAudioReader.iterator()
        ) {
            T constantFrameDataValue = null;
            while (imageIterator.hasNext()) {
                var imageFrame = imageIterator.next();
                if (constantFrameDataValue == null) {
                    constantFrameDataValue = processor.constantData(imageFrame.content());
                }
                writer.writeImageFrame(imageFrame.transform(
                    processor.transformImage(imageFrame, constantFrameDataValue),
                    processor.absoluteSpeed()
                ));
            }
            while (audioIterator.hasNext()) {
                var audioFrame = audioIterator.next();
                writer.writeAudioFrame(audioFrame.transform(processor.absoluteSpeed()));
            }
            return output;
        }
    }

    public static <T> File processMedia(
        MediaReader<ImageFrame> imageReader1,
        MediaReader<AudioFrame> audioReader1,
        MediaReader<ImageFrame> imageReader2,
        String outputFormat,
        String resultName,
        DualImageProcessor<T> processor
    ) throws IOException {
        var output = FileUtil.createTempFile(resultName, outputFormat);
        var zippedImageReader = new ZippedMediaReader<>(imageReader1, imageReader2);
        if (processor.speed() < 0) {
            zippedImageReader = zippedImageReader.reversed();
            audioReader1 = audioReader1.reversed();
        }
        try (
            processor;
            var finalZippedImageReader = zippedImageReader;
            var finalAudioReader = audioReader1;
            var writer = MediaWriters.createWriter(
                output,
                outputFormat,
                finalAudioReader.audioChannels(),
                finalAudioReader.audioSampleRate(),
                finalAudioReader.audioBitrate());
            var zippedImageIterator = finalZippedImageReader.iterator();
            var audioIterator = finalAudioReader.iterator()
        ) {
            T constantFrameDataValue = null;
            while (zippedImageIterator.hasNext()) {
                var framePair = zippedImageIterator.next();
                var firstFrame = framePair.first();
                var secondFrame = framePair.second();
                if (constantFrameDataValue == null) {
                    constantFrameDataValue = processor.constantData(firstFrame.content(), secondFrame.content());
                }
                var toTransform = finalZippedImageReader.isFirstControlling()
                    ? firstFrame
                    : secondFrame;
                writer.writeImageFrame(toTransform.transform(
                    processor.transformImage(firstFrame, secondFrame, constantFrameDataValue),
                    processor.absoluteSpeed()
                ));
            }
            while (audioIterator.hasNext()) {
                var audioFrame = audioIterator.next();
                writer.writeAudioFrame(audioFrame.transform(processor.absoluteSpeed()));
            }
            return output;
        }
    }

    public static File cropMedia(
        File media,
        String outputFormat,
        String resultName,
        Function<BufferedImage, Rectangle> cropKeepAreaFinder
    ) throws IOException {
        try (
            var reader = MediaReaders.createImageReader(media, outputFormat);
            var iterator = reader.iterator()
        ) {
            Rectangle toKeep = null;
            var width = -1;
            var height = -1;

            while (iterator.hasNext()) {
                var frame = iterator.next();
                var image = frame.content();
                if (width < 0) {
                    width = image.getWidth();
                    height = image.getHeight();
                }

                var mayKeepArea = cropKeepAreaFinder.apply(image);
                if ((mayKeepArea.getX() != 0
                    || mayKeepArea.getY() != 0
                    || mayKeepArea.getWidth() != width
                    || mayKeepArea.getHeight() != height)
                    && mayKeepArea.getWidth() > 0
                    && mayKeepArea.getHeight() > 0
                ) {
                    if (toKeep == null) {
                        toKeep = mayKeepArea;
                    } else {
                        toKeep = toKeep.union(mayKeepArea);
                    }
                }
            }

            if (toKeep == null
                || (toKeep.getX() == 0
                && toKeep.getY() == 0
                && toKeep.getWidth() == width
                && toKeep.getHeight() == height
            )) {
                return media;
            } else {
                var finalToKeep = toKeep;
                return processMedia(
                    media,
                    outputFormat,
                    resultName,
                    image -> image.getSubimage(
                        finalToKeep.x,
                        finalToKeep.y,
                        finalToKeep.width,
                        finalToKeep.height
                    )
                );
            }
        }
    }

    public static String equivalentTransparentFormat(String format) {
        if (isJpg(format)) {
            return "png";
        } else {
            return format;
        }
    }

    public static int supportedTransparentImageType(BufferedImage image, String format) {
        return supportsTransparency(format) ? BufferedImage.TYPE_INT_ARGB : ImageUtil.getType(image);
    }

    private static boolean supportsTransparency(String format) {
        return equalsIgnoreCaseAny(format,
            "png",
            "gif"
        );
    }

    private static boolean isJpg(String format) {
        return equalsIgnoreCaseAny(format,
            "jpg",
            "jpeg"
        );
    }

    public static boolean isStaticOnly(String format) {
        return equalsIgnoreCaseAny(format,
            "bmp",
            "jpeg",
            "jpg",
            "wbmp",
            "png",
            "tif",
            "tiff"
        );
    }

    private static boolean equalsIgnoreCaseAny(String string, String... toCompare) {
        for (var compare : toCompare) {
            if (string.equalsIgnoreCase(compare)) {
                return true;
            }
        }
        return false;
    }

    public static <E extends VideoFrame<?>> E frameAtTime(long timestamp, List<E> frames, long duration) {
        var circularTimestamp = timestamp % Math.max(duration, 1);
        var index = findIndex(circularTimestamp, frames);
        return frames.get(index);
    }

    /**
     * Finds the index of the frame with the given timestamp.
     * If there is no frame with the given timestamp, the index of the frame
     * with the highest timestamp smaller than the given timestamp is returned.
     *
     * @param timeStamp The timestamp in microseconds.
     * @param frames    The frames.
     * @return The index of the frame with the given timestamp.
     */
    private static int findIndex(long timeStamp, List<? extends VideoFrame<?>> frames) {
        if (frames.size() == 0) {
            throw new IllegalArgumentException("Frames list is empty");
        } else if (timeStamp < 0) {
            throw new IllegalArgumentException("Timestamp must not be negative");
        } else if (timeStamp < frames.get(0).timestamp()) {
            throw new IllegalArgumentException("Timestamp must not be smaller than the first timestamp");
        } else if (timeStamp == frames.get(0).timestamp()) {
            return 0;
        } else if (timeStamp < frames.get(frames.size() - 1).timestamp()) {
            return findIndexBinarySearch(timeStamp, frames);
        } else {
            // If the timestamp is equal to or greater than the last timestamp.
            return frames.size() - 1;
        }
    }

    private static int findIndexBinarySearch(long timeStamp, List<? extends VideoFrame<?>> frames) {
        var low = 0;
        var high = frames.size() - 1;
        while (low <= high) {
            var mid = low + ((high - low) / 2);
            if (frames.get(mid).timestamp() == timeStamp
                || (frames.get(mid).timestamp() < timeStamp
                && frames.get(mid + 1).timestamp() > timeStamp)) {
                return mid;
            } else if (frames.get(mid).timestamp() < timeStamp) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        throw new IllegalStateException("This should never be reached. Timestamp: " + timeStamp + ", Frames: " + frames);
    }
}
