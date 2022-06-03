package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SpeedCommand extends MediaCommand {

    public static final float DEFAULT_SPEED_MULTIPLIER = 2;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public SpeedCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public File applyOperation(File media, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        float speedMultiplier = CommandParser.parseFloatArgument(
                arguments,
                0,
                DEFAULT_SPEED_MULTIPLIER,
                event.getChannel(),
                (argument, defaultValue) -> "Speed multiplier \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );
        return manipulator.speed(media, fileFormat, speedMultiplier);
    }
}
