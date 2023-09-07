package io.github.shaksternano.borgar.core.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.core.command.util.CommandParser;
import io.github.shaksternano.borgar.core.io.NamedFile;
import io.github.shaksternano.borgar.core.media.ImageUtil;
import io.github.shaksternano.borgar.core.media.MediaUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A command that stretches media.
 */
public class StretchCommand extends FileCommand {

    /**
     * The default stretch width multiplier.
     */
    public static final float DEFAULT_WIDTH_MULTIPLIER = 2;

    /**
     * The default stretch height multiplier.
     */
    public static final float DEFAULT_HEIGHT_MULTIPLIER = 1;

    private final boolean RAW;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public StretchCommand(String name, String description, boolean raw) {
        super(name, description);
        RAW = raw;
    }

    /**
     * Stretches media. The stretch width multiplier is specified by the first element of the arguments array,
     * with a default value of {@link #DEFAULT_WIDTH_MULTIPLIER} if it is not specified or un-parsable,
     * and the stretch height multiplier is specified by the second element of the arguments array,
     * with a default value of {@link #DEFAULT_HEIGHT_MULTIPLIER} if it is not specified or un-parsable.
     *
     * @param file           The media file to apply the operation to.
     * @param fileName
     * @param fileFormat     The file format of the media file.
     * @param arguments      The arguments of the command.
     * @param extraArguments A multimap mapping the additional parameter names to a list of the arguments.
     * @param event          The {@link MessageReceivedEvent} that triggered the command.
     * @param maxFileSize    The maximum file size of the output file.
     * @return The edited media file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @Override
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        float widthMultiplier = CommandParser.parseFloatArgument(
            arguments,
            0,
            DEFAULT_WIDTH_MULTIPLIER,
            null,
            event.getChannel(),
            (argument, defaultValue) -> "Width multiplier \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );
        float heightMultiplier = CommandParser.parseFloatArgument(
            arguments,
            1,
            DEFAULT_HEIGHT_MULTIPLIER,
            null,
            event.getChannel(),
            (argument, defaultValue) -> "Height multiplier \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );
        return new NamedFile(
            MediaUtil.processMedia(
                file,
                fileFormat,
                "stretch",
                image -> ImageUtil.stretch(
                    image,
                    (int) (image.getWidth() * widthMultiplier),
                    (int) (image.getHeight() * heightMultiplier),
                    RAW
                ),
                maxFileSize
            ),
            "stretched",
            fileFormat
        );
    }
}
