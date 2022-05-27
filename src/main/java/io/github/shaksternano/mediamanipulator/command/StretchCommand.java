package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;

/**
 * A command that stretches media.
 */
public class StretchCommand extends MediaCommand {

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
     * @param media       The media file to apply the operation to.
     * @param fileFormat  The file format of the media file.
     * @param arguments   The arguments of the command.
     * @param manipulator The {@link MediaManipulator} to use for the operation.
     * @param event       The {@link MessageReceivedEvent} that triggered the command.
     * @return The edited media file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @Override
    public File applyOperation(File media, String fileFormat, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        float widthMultiplier = CommandParser.parseFloatArgument(
                arguments,
                0,
                DEFAULT_WIDTH_MULTIPLIER,
                event.getChannel(),
                (argument, defaultValue) -> "Width multiplier \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );
        float heightMultiplier = CommandParser.parseFloatArgument(
                arguments,
                1,
                DEFAULT_HEIGHT_MULTIPLIER,
                event.getChannel(),
                (argument, defaultValue) -> "Height multiplier \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );

        return manipulator.stretch(media, fileFormat, widthMultiplier, heightMultiplier, RAW);
    }
}
