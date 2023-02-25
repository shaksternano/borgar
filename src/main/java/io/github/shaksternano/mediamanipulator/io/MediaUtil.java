package io.github.shaksternano.mediamanipulator.io;

import io.github.shaksternano.mediamanipulator.image.ImageFrame;
import io.github.shaksternano.mediamanipulator.image.ImageProcessor;
import io.github.shaksternano.mediamanipulator.io.mediareader.MediaReader;
import io.github.shaksternano.mediamanipulator.io.mediawriter.MediaWriter;
import org.bytedeco.javacv.Frame;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class MediaUtil {

    public static File processMedia(
        File media,
        String outputFormat,
        String operationName,
        UnaryOperator<BufferedImage> imageMapper
    ) throws IOException {
        return processMedia(
            media,
            outputFormat,
            operationName,
            new BasicImageProcessor(imageMapper)
        );
    }

    public static <T> File processMedia(
        File media,
        String outputFormat,
        String operationName,
        ImageProcessor<T> processor
    ) throws IOException {
        String outputName = operationName + "." + outputFormat;
        File output = FileUtil.getUniqueTempFile(outputName);
        try (
            MediaReader<ImageFrame> imageReader = MediaReaders.createImageReader(media, outputFormat);
            MediaReader<Frame> audioReader = MediaReaders.createAudioReader(media, outputFormat);
            MediaWriter writer = MediaWriters.createWriter(
                output,
                outputFormat,
                audioReader.getAudioChannels()
            )
        ) {
            T globalFrameDataValue = null;
            for (ImageFrame imageFrame : imageReader) {
                long timestamp = imageFrame.timestamp();
                if (globalFrameDataValue == null) {
                    globalFrameDataValue = processor.globalData(imageFrame.image());
                }
                writer.recordImageFrame(new ImageFrame(
                    processor.transformImage(imageFrame, globalFrameDataValue),
                    imageFrame.duration(),
                    timestamp
                ));
            }
            for (Frame audioFrame : audioReader) {
                writer.recordAudioFrame(audioFrame);
            }
        }
        return output;
    }

    public static File cropMedia(
        File media,
        String outputFormat,
        String operationName,
        Function<BufferedImage, Rectangle> cropKeepAreaFinder
    ) throws IOException {
        try (MediaReader<ImageFrame> reader = MediaReaders.createImageReader(media, outputFormat)) {
            Rectangle toKeep = null;
            int width = -1;
            int height = -1;

            for (ImageFrame frame : reader) {
                BufferedImage image = frame.image();
                if (width < 0) {
                    width = image.getWidth();
                    height = image.getHeight();
                }

                Rectangle mayKeepArea = cropKeepAreaFinder.apply(image);
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
                final Rectangle finalToKeep = toKeep;
                return processMedia(
                    media,
                    outputFormat,
                    operationName,
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
        if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
            return "png";
        } else {
            return format;
        }
    }

    private record BasicImageProcessor(UnaryOperator<BufferedImage> imageMapper) implements ImageProcessor<Boolean> {

        @Override
        public BufferedImage transformImage(ImageFrame frame, Boolean globalData) {
            return imageMapper.apply(frame.image());
        }

        @Override
        public Boolean globalData(BufferedImage image) {
            return Boolean.TRUE;
        }
    }
}
