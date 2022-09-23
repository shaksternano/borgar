package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A command that adds a captions media.
 */
public class CaptionCommand extends MediaCommand {

    private final boolean CAPTION_2;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     * @param caption2    Whether to put text on the bottom of the image instead of the top.
     */
    public CaptionCommand(String name, String description, boolean caption2) {
        super(name, description);
        CAPTION_2 = caption2;
    }

    @Override
    public File applyOperation(File media, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        Map<String, Drawable> nonTextParts = MessageUtil.getEmojiImages(event.getMessage());
        return manipulator.caption(media, fileFormat, arguments, nonTextParts, CAPTION_2);
    }
}
