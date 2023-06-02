package io.github.shaksternano.mediamanipulator.media;

import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.media.io.MediaReaders;
import io.github.shaksternano.mediamanipulator.media.io.MediaWriters;
import io.github.shaksternano.mediamanipulator.media.io.imageprocessor.BasicImageProcessor;
import io.github.shaksternano.mediamanipulator.media.io.imageprocessor.DualImageProcessor;
import io.github.shaksternano.mediamanipulator.media.io.imageprocessor.IdentityProcessor;
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
        File input,
        String outputFormat,
        String resultName,
        UnaryOperator<BufferedImage> imageMapper,
        long maxFileSize
    ) throws IOException {
        return processMedia(
            input,
            outputFormat,
            resultName,
            new BasicImageProcessor(imageMapper),
            maxFileSize
        );
    }

    public static File processMedia(
        File input,
        String outputFormat,
        String resultName,
        SingleImageProcessor<?> processor,
        long maxFileSize
    ) throws IOException {
        var output = FileUtil.createTempFile(resultName, outputFormat);
        return processMedia(input, output, outputFormat, processor, maxFileSize);
    }

    public static File processMedia(
        File input,
        File output,
        String outputFormat,
        SingleImageProcessor<?> processor,
        long maxFileSize
    ) throws IOException {
        return processMedia(
            input,
            outputFormat,
            output,
            outputFormat,
            processor,
            maxFileSize
        );
    }

    public static File processMedia(
        File input,
        String inputFormat,
        File output,
        String outputFormat,
        SingleImageProcessor<?> processor,
        long maxFileSize
    ) throws IOException {
        var imageReader = MediaReaders.createImageReader(input, inputFormat);
        var audioReader = MediaReaders.createAudioReader(input, inputFormat);
        return processMedia(imageReader, audioReader, output, outputFormat, processor, maxFileSize);
    }

    public static File processMedia(
        MediaReader<ImageFrame> imageReader,
        MediaReader<AudioFrame> audioReader,
        String outputFormat,
        String resultName,
        SingleImageProcessor<?> processor,
        long maxFileSize
    ) throws IOException {
        var output = FileUtil.createTempFile(resultName, outputFormat);
        return processMedia(imageReader, audioReader, output, outputFormat, processor, maxFileSize);
    }

    public static <T> File processMedia(
        MediaReader<ImageFrame> imageReader,
        MediaReader<AudioFrame> audioReader,
        File output,
        String outputFormat,
        SingleImageProcessor<T> processor,
        long maxFileSize
    ) throws IOException {
        if (processor.speed() < 0) {
            imageReader = imageReader.reversed();
            audioReader = audioReader.reversed();
        }
        try (
            var finalImageReader = imageReader;
            var finalAudioReader = audioReader;
            processor
        ) {
            var outputSize = Long.MAX_VALUE;
            var resizeRatio = 1F;
            var maxResizeAttempts = 3;
            var attempts = 0;
            do {
                try (
                    var imageIterator = finalImageReader.iterator();
                    var audioIterator = finalAudioReader.iterator();
                    var writer = MediaWriters.createWriter(
                        output,
                        outputFormat,
                        finalAudioReader.audioChannels(),
                        finalAudioReader.audioSampleRate(),
                        finalAudioReader.audioBitrate(),
                        maxFileSize,
                        finalImageReader.duration()
                    )
                ) {
                    T constantFrameDataValue = null;
                    while (imageIterator.hasNext()) {
                        var imageFrame = imageIterator.next();
                        if (constantFrameDataValue == null) {
                            constantFrameDataValue = processor.constantData(imageFrame.content());
                        }
                        writer.writeImageFrame(imageFrame.transform(
                            ImageUtil.resize(
                                processor.transformImage(imageFrame, constantFrameDataValue),
                                resizeRatio
                            ),
                            processor.absoluteSpeed()
                        ));
                        if (writer.isStatic()) {
                            break;
                        }
                    }

                    if (writer.supportsAudio()) {
                        while (audioIterator.hasNext()) {
                            var audioFrame = audioIterator.next();
                            writer.writeAudioFrame(audioFrame.transform(processor.absoluteSpeed()));
                        }
                    }
                }
                outputSize = output.length();
                resizeRatio = Math.min((float) maxFileSize / outputSize, 0.9F);
                attempts++;
            } while (maxFileSize > 0 && outputSize > maxFileSize && attempts < maxResizeAttempts);
            return output;
        }
    }

    public static <T> File processMedia(
        MediaReader<ImageFrame> imageReader1,
        MediaReader<AudioFrame> audioReader1,
        MediaReader<ImageFrame> imageReader2,
        String outputFormat,
        String resultName,
        DualImageProcessor<T> processor,
        long maxFileSize
    ) throws IOException {
        var output = FileUtil.createTempFile(resultName, outputFormat);
        var zippedImageReader = new ZippedMediaReader<>(imageReader1, imageReader2);
        if (processor.speed() < 0) {
            zippedImageReader = zippedImageReader.reversed();
            audioReader1 = audioReader1.reversed();
        }
        try (
            var finalZippedImageReader = zippedImageReader;
            var finalAudioReader = audioReader1;
            processor
        ) {
            var outputSize = Long.MAX_VALUE;
            var resizeRatio = 1F;
            var maxResizeAttempts = 3;
            var attempts = 0;
            do {
                try (
                    var zippedImageIterator = finalZippedImageReader.iterator();
                    var audioIterator = finalAudioReader.iterator();
                    var writer = MediaWriters.createWriter(
                        output,
                        outputFormat,
                        finalAudioReader.audioChannels(),
                        finalAudioReader.audioSampleRate(),
                        finalAudioReader.audioBitrate(),
                        maxFileSize,
                        finalZippedImageReader.duration()
                    )
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
                            ImageUtil.resize(
                                processor.transformImage(firstFrame, secondFrame, constantFrameDataValue),
                                resizeRatio
                            ),
                            processor.absoluteSpeed()
                        ));
                        if (writer.isStatic()) {
                            break;
                        }
                    }

                    if (writer.supportsAudio()) {
                        while (audioIterator.hasNext()) {
                            var audioFrame = audioIterator.next();
                            writer.writeAudioFrame(audioFrame.transform(processor.absoluteSpeed()));
                        }
                    }
                }
                outputSize = output.length();
                resizeRatio = Math.min((float) maxFileSize / outputSize, 0.9F);
                attempts++;
            } while (maxFileSize > 0 && outputSize > maxFileSize && attempts < maxResizeAttempts);
            return output;
        }
    }

    public static File cropMedia(
        File media,
        String outputFormat,
        String resultName,
        Function<BufferedImage, Rectangle> cropKeepAreaFinder,
        long maxFileSize
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
                    ),
                    maxFileSize
                );
            }
        }
    }

    public static File transcode(
        File input,
        String inputFormat,
        String outputFormat,
        long maxFileSize
    ) throws IOException {
        var nameWithoutExtension = Files.getNameWithoutExtension(input.getName());
        var output = FileUtil.createTempFile(nameWithoutExtension, outputFormat);
        return processMedia(
            input,
            inputFormat,
            output,
            outputFormat,
            IdentityProcessor.INSTANCE,
            maxFileSize
        );
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

    public static boolean supportsTransparency(String format) {
        return equalsIgnoreCaseAny(format,
            "bmp",
            "png",
            "gif",
            "tif",
            "tiff"
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

    public static <E extends VideoFrame<?, E>> E frameAtTime(long timestamp, List<E> frames, long duration) {
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
    private static int findIndex(long timeStamp, List<? extends VideoFrame<?, ?>> frames) {
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

    private static int findIndexBinarySearch(long timeStamp, List<? extends VideoFrame<?, ?>> frames) {
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
