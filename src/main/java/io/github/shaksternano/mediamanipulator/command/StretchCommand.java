package io.github.shaksternano.mediamanipulator.command;

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
     * @param name        The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
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
     * @param media       The media file to apply the operation to
     * @param arguments   The arguments of the command.
     * @param manipulator The {@link MediaManipulator} to use for the operation.
     * @param event       The {@link MessageReceivedEvent} that triggered the command.
     * @return The edited media file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @Override
    public File applyOperation(File media, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        float widthMultiplier = DEFAULT_WIDTH_MULTIPLIER;
        float heightMultiplier = DEFAULT_HEIGHT_MULTIPLIER;

        if (arguments.length > 0) {
            try {
                widthMultiplier = Float.parseFloat(arguments[0]);
                if (arguments.length > 1) {
                    try {
                        heightMultiplier = Float.parseFloat(arguments[1]);
                    } catch (NumberFormatException e) {
                        event.getMessage().reply("Height multiplier \"" + arguments[0] + "\" is not a number. Using default value of " + widthMultiplier + ".").queue();
                    }
                }
            } catch (NumberFormatException e) {
                event.getMessage().reply("Width multiplier \"" + arguments[0] + "\" is not a number. Using default value of " + widthMultiplier + ".").queue();
            }
        }

        return manipulator.stretch(media, widthMultiplier, heightMultiplier, RAW);
    }
}
