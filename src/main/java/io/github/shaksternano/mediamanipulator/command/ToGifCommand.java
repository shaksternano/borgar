package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Turns media into a GIF.
 */
public class ToGifCommand extends MediaCommand {

    private final boolean JUST_RENAME_FILE;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
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
     * @param media          The media file to apply the operation to.
     * @param fileFormat     The file format of the media file.
     * @param arguments      The arguments of the command.
     * @param extraArguments A multimap mapping the additional parameter names to a list of the arguments.
     * @param manipulator    The {@link MediaManipulator} to use for the operation.
     * @param event          The {@link MessageReceivedEvent} that triggered the command.
     * @return The edited media file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @Override
    public File applyOperation(File media, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        return manipulator.makeGif(media, fileFormat, JUST_RENAME_FILE);
    }
}
