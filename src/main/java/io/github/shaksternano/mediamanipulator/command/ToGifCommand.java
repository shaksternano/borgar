package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;

/**
 * Turns media into a GIF.
 */
public class ToGifCommand extends MediaCommand {

    private boolean JUST_RENAME_FILE;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ToGifCommand(String name, String description, boolean justRenameFile) {
        super(name, description);
        JUST_RENAME_FILE = justRenameFile;
    }

    /**
     * Turns media into a GIF.
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
        return manipulator.makeGif(media, JUST_RENAME_FILE);
    }
}
