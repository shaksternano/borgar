package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;

/**
 * Resizes an image by a certain amount.
 */
public class ResizeCommand extends MediaCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link CommandParser#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ResizeCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Resizes an image by the amount specified in the first argument.
     * Equivalent to stretching an image with the width and height multipliers set to the same amount.
     *
     * @param mediaFile   The media file to apply the operation to
     * @param arguments   The arguments of the command.
     * @param manipulator The {@link MediaManipulator} to use for the operation.
     * @param event       The {@link MessageReceivedEvent} that triggered the command.
     * @return The edited media file.
     * @throws IOException              If an error occurs while applying the operation.
     * @throws IllegalArgumentException If an argument is invalid.
     * @throws MissingArgumentException If the operation requires an argument but none was provided.
     */
    @Override
    public File applyOperation(File mediaFile, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        if (arguments.length > 0) {
            try {
                float resizeMultiplier = Float.parseFloat(arguments[0]);
                return manipulator.stretch(mediaFile, resizeMultiplier, resizeMultiplier);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Scale multiplier \"" + arguments[0] + "\" is not a number!");
            }
        } else {
            throw new MissingArgumentException("Please specify a scale multiplier!");
        }
    }
}
