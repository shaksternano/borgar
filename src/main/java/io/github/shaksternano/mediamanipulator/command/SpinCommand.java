package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.io.NamedFile;
import io.github.shaksternano.mediamanipulator.media.ImageFrame;
import io.github.shaksternano.mediamanipulator.media.ImageUtil;
import io.github.shaksternano.mediamanipulator.media.MediaUtil;
import io.github.shaksternano.mediamanipulator.media.io.MediaReaders;
import io.github.shaksternano.mediamanipulator.media.io.MediaWriters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
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
    protected NamedFile modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
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
        return spin(file, fileFormat, spinSpeed, rgb < 0 ? null : new Color(rgb));
    }

    private static NamedFile spin(File media, String format, float speed, @Nullable Color backgroundColor) throws IOException {
        var outputFormat = MediaUtil.isStaticOnly(format) ? "gif" : format;
        var output = FileUtil.createTempFile("spun", outputFormat);
        try (
            var imageReader = MediaReaders.createImageReader(media, format);
            var audioReader = MediaReaders.createAudioReader(media, format);
            var writer = MediaWriters.createWriter(output, outputFormat, audioReader.audioChannels());
            var audioIterator = audioReader.iterator()
        ) {
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
            var imageType = -1;
            for (var timestamp = 0L; timestamp < totalDuration; timestamp += frameDuration) {
                var image = imageReader.frameAtTime(timestamp).content();
                if (imageType < 0) {
                    imageType = MediaUtil.supportedTransparentImageType(image, imageReader.format());
                }
                var angle = 2 * Math.PI * (timestamp / (double) rotationDuration);
                if (speed < 0) {
                    angle = -angle;
                }
                var rotatedImage = ImageUtil.rotate(
                    image,
                    angle,
                    maxDimension,
                    maxDimension,
                    backgroundColor,
                    imageType
                );
                writer.recordImageFrame(new ImageFrame(rotatedImage, frameDuration, timestamp));
            }
            while (audioIterator.hasNext()) {
                var audioFrame = audioIterator.next();
                writer.recordAudioFrame(audioFrame);
            }
            return new NamedFile(output, "spun", outputFormat);
        }
    }
}
