package io.github.shaksternano.mediamanipulator.io;

import io.github.shaksternano.mediamanipulator.io.mediareader.FFmpegAudioReader;
import io.github.shaksternano.mediamanipulator.io.mediareader.FFmpegImageReader;
import io.github.shaksternano.mediamanipulator.io.mediareader.MediaReader;
import io.github.shaksternano.mediamanipulator.io.mediawriter.MediaWriter;
import org.bytedeco.javacv.Frame;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public class MediaUtil {

    public static File processMedia(
        File media,
        String outputFormat,
        String operationName,
        Function<BufferedImage, BufferedImage> imageMapper
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
            MediaReader<BufferedImage> imageReader = new FFmpegImageReader(media);
            MediaReader<Frame> audioReader = new FFmpegAudioReader(media);
            MediaWriter writer = MediaWriters.createWriter(
                output,
                outputFormat,
                imageReader.getFrameRate(),
                audioReader.getAudioChannels()
            )
        ) {
            T globalFrameDataValue = null;
            for (BufferedImage imageFrame : imageReader) {
                if (globalFrameDataValue == null) {
                    globalFrameDataValue = processor.globalData(imageFrame);
                }
                writer.recordImageFrame(processor.transformImage(imageFrame, globalFrameDataValue));
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
        try (MediaReader<BufferedImage> reader = new FFmpegImageReader(media)) {
            Rectangle toKeep = null;
            int width = -1;
            int height = -1;

            for (BufferedImage frame : reader) {
                if (width < 0) {
                    width = frame.getWidth();
                    height = frame.getHeight();
                }

                Rectangle mayKeepArea = cropKeepAreaFinder.apply(frame);
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

    private record BasicImageProcessor(Function<BufferedImage, BufferedImage> imageMapper) implements ImageProcessor<Boolean> {

        @Override
        public BufferedImage transformImage(BufferedImage image, Boolean extraData) {
            return imageMapper.apply(image);
        }

        @Override
        public Boolean globalData(BufferedImage image) {
            return Boolean.TRUE;
        }
    }
}
