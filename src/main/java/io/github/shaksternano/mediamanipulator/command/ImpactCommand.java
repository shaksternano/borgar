package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.exception.MissingArgumentException;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImpactCommand extends MediaCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ImpactCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public File applyOperation(File media, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        Map<String, Drawable> nonTextParts = MessageUtil.getEmojiImages(event.getMessage());
        List<String> bottomWords = extraArguments.get("bottom");
        if (arguments.isEmpty() && bottomWords.isEmpty()) {
            throw new MissingArgumentException("Please specify text!");
        } else {
            return manipulator.impact(media, fileFormat, arguments, bottomWords, nonTextParts);
        }
    }

    @Override
    public Set<String> getAdditionalParameterNames() {
        return ImmutableSet.of(
                "bottom"
        );
    }
}
