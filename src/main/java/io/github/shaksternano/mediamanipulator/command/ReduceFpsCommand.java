package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;

public class ReduceFpsCommand extends MediaCommand {

    public static final int DEFAULT_FPS_REDUCTION_MULTIPLIER = 2;


    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ReduceFpsCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public File applyOperation(File media, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        int fpsReductionMultiplier = CommandParser.parseIntegerArgument(
                arguments,
                0,
                DEFAULT_FPS_REDUCTION_MULTIPLIER,
                event.getChannel(),
                (argument, defaultValue) -> "FPS reduction multiplier \"" + argument + "\" is not a whole number. Using default value of " + defaultValue + "."
        );
        return manipulator.reduceFps(media, fpsReductionMultiplier);
    }
}
