package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.exception.MissingArgumentException;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class CropCommand extends MediaCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public CropCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public File applyOperation(File media, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        MessageChannel triggerChannel = event.getChannel();
        float topRatio = CommandParser.parseFloatExtraArgument(
                extraArguments,
                "top",
                0,
                result -> result >= 0 && result < 1,
                triggerChannel,
                (argument, defaultValue) -> "Top crop ratio \"" + argument + "\" is not a positive number between 0 inclusive and 1 exclusive, ignoring value!"
        );
        float rightRatio = CommandParser.parseFloatExtraArgument(
                extraArguments,
                "right",
                0,
                result -> result >= 0 && result < 1,
                triggerChannel,
                (argument, defaultValue) -> "Right crop ratio \"" + argument + "\" is not a positive number between 0 inclusive and 1 exclusive, ignoring value!"
        );
        float bottomRatio = CommandParser.parseFloatExtraArgument(
                extraArguments,
                "bottom",
                0,
                result -> result >= 0 && result < 1,
                triggerChannel,
                (argument, defaultValue) -> "Bottom crop ratio \"" + argument + "\" is not a positive number between 0 inclusive and 1 exclusive, ignoring value!"
        );
        float leftRatio = CommandParser.parseFloatExtraArgument(
                extraArguments,
                "left",
                0,
                result -> result >= 0 && result < 1,
                triggerChannel,
                (argument, defaultValue) -> "Left crop ratio \"" + argument + "\" is not a positive number between 0 inclusive and 1 exclusive, ignoring value!"
        );

        if (topRatio == 0 && rightRatio == 0 && bottomRatio == 0 && leftRatio == 0) {
            throw new MissingArgumentException("No valid crop ratios were specified! Please specify at least one valid crop ratio.");
        } else {
            return manipulator.crop(media, fileFormat, topRatio, rightRatio, bottomRatio, leftRatio);
        }
    }

    @Override
    public Set<String> getAdditionalParameterNames() {
        return ImmutableSet.of(
                "top",
                "right",
                "bottom",
                "left"
        );
    }
}
