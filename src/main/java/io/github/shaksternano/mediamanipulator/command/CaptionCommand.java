package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;

/**
 * A command that adds a captions media.
 */
public class CaptionCommand extends MediaCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link CommandParser#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    protected CaptionCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Adds a caption to media, with the caption text being the arguments of the command joined together with spaces in between.
     * @param mediaFile The media file to apply the operation to
     * @param arguments The arguments of the command.
     * @param manipulator The {@link MediaManipulator} to use for the operation.
     * @param event The {@link MessageReceivedEvent} that triggered the command.
     * @return The edited media file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @Override
    public File applyOperation(File mediaFile, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        return manipulator.caption(mediaFile, String.join(" ", arguments));
    }
}
