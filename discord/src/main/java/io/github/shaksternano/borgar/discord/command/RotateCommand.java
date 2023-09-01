package io.github.shaksternano.borgar.discord.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.discord.command.util.CommandParser;
import io.github.shaksternano.borgar.discord.io.NamedFile;
import io.github.shaksternano.borgar.discord.media.ImageUtil;
import io.github.shaksternano.borgar.discord.media.MediaUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class RotateCommand extends FileCommand {

    public static final float DEFAULT_ROTATION = 90;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public RotateCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        var rotationDegrees = CommandParser.parseFloatArgument(
            arguments,
            0,
            DEFAULT_ROTATION,
            null,
            event.getChannel(),
            (argument, defaultValue) -> "Rotation \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );
        var rgb = CommandParser.parseIntegerArgument(
            arguments,
            1,
            -1,
            null,
            event.getChannel(),
            (argument, defaultValue) -> "RGB value \"" + argument + "\" is not a whole number. Setting transparent background color."
        );
        var backgroundColor = rgb < 0 ? null : new Color(rgb);
        var outputFormat = MediaUtil.equivalentTransparentFormat(fileFormat);
        return new NamedFile(
            MediaUtil.processMedia(
                file,
                outputFormat,
                "rotated",
                image -> rotate(image, rotationDegrees, backgroundColor, outputFormat),
                maxFileSize
            ),
            "rotated",
            outputFormat
        );
    }

    private static BufferedImage rotate(BufferedImage image, double degrees, @Nullable Color backgroundColor, String format) {
        var resultType = MediaUtil.supportedTransparentImageType(image, format);
        var radians = Math.toRadians(degrees);
        return ImageUtil.rotate(image, radians, null, null, backgroundColor, resultType);
    }
}
