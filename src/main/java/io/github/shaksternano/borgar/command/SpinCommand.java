package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.command.util.CommandParser;
import io.github.shaksternano.borgar.io.FileUtil;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.media.ImageFrame;
import io.github.shaksternano.borgar.media.ImageUtil;
import io.github.shaksternano.borgar.media.MediaUtil;
import io.github.shaksternano.borgar.media.io.MediaReaders;
import io.github.shaksternano.borgar.media.io.imageprocessor.SingleImageProcessor;
import io.github.shaksternano.borgar.media.io.reader.ConstantFrameDurationReader;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SpinCommand extends FileCommand {

    public static final int DEFAULT_SPIN_SPEED = 1;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public SpinCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected NamedFile modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        var spinSpeed = CommandParser.parseFloatArgument(arguments,
            0,
            DEFAULT_SPIN_SPEED,
            null,
            event.getChannel(),
            (argument, defaultValue) -> "Spin speed \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );
        var rgb = CommandParser.parseIntegerArgument(
            arguments,
            1,
            -1,
            null,
            event.getChannel(),
            (argument, defaultValue) -> "RGB value \"" + argument + "\" is not a whole number. Setting transparent background color."
        );
        return spin(
            file,
            fileFormat,
            spinSpeed,
            rgb < 0 ? null : new Color(rgb),
            maxFileSize
        );
    }

    private static NamedFile spin(File media, String format, float speed, @Nullable Color backgroundColor, long maxFileSize) throws IOException {
        var outputFormat = MediaUtil.isStaticOnly(format) ? "gif" : format;
        var nameWithoutExtension = "spun";
        var output = FileUtil.createTempFile(nameWithoutExtension, outputFormat);
        var imageReader = MediaReaders.createImageReader(media, format);
        var audioReader = MediaReaders.createAudioReader(media, format);
        var mediaDuration = imageReader.duration();
        var absoluteSpeed = Math.abs(speed);
        var frameDuration = 20000L;
        var framesPerRotation = 150;
        if (absoluteSpeed > 1) {
            framesPerRotation = Math.max((int) (framesPerRotation / absoluteSpeed), 1);
        }
        var rotationDuration = frameDuration * framesPerRotation;
        var rotations = (int) Math.ceil(mediaDuration / (double) rotationDuration);
        var totalDuration = rotations * rotationDuration;
        var maxDimension = Math.max(imageReader.width(), imageReader.height());
        return new NamedFile(
            MediaUtil.processMedia(
                new ConstantFrameDurationReader<>(imageReader, frameDuration, totalDuration),
                audioReader,
                output,
                format,
                new SpinProcessor(
                    format,
                    speed,
                    rotationDuration,
                    maxDimension,
                    backgroundColor
                ),
                maxFileSize
            ),
            nameWithoutExtension,
            outputFormat
        );
    }

    private record SpinProcessor(
        String outputFormat,
        float speed,
        long rotationDuration,
        int maxDimension,
        @Nullable Color backgroundColor
    ) implements SingleImageProcessor<SpinData> {

        @Override
        public BufferedImage transformImage(ImageFrame frame, SpinData constantData) {
            var image = frame.content();
            var angle = 2 * Math.PI * (frame.timestamp() / (double) rotationDuration);
            if (speed < 0) {
                angle = -angle;
            }
            return ImageUtil.rotate(
                image,
                angle,
                maxDimension,
                maxDimension,
                backgroundColor,
                constantData.imageType()
            );
        }

        @Override
        public SpinData constantData(BufferedImage image) {
            return new SpinData(
                MediaUtil.supportedTransparentImageType(image, outputFormat)
            );
        }
    }

    private record SpinData(
        int imageType
    ) {
    }
}
