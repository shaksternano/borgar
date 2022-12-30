package io.github.shaksternano.mediamanipulator.io;

import io.github.shaksternano.mediamanipulator.io.mediareader.FFmpegAudioReader;
import io.github.shaksternano.mediamanipulator.io.mediareader.FFmpegImageReader;
import io.github.shaksternano.mediamanipulator.io.mediareader.MediaReader;
import io.github.shaksternano.mediamanipulator.io.mediawriter.MediaWriter;
import org.bytedeco.javacv.Frame;

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
            for (BufferedImage imageFrame : imageReader) {
                writer.recordImageFrame(imageMapper.apply(imageFrame));
            }
            for (Frame audioFrame : audioReader) {
                writer.recordAudioFrame(audioFrame);
            }
        }
        return output;
    }
}
