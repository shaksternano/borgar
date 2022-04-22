package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;

public class PixelateCommand extends MediaCommand {

    public static final int DEFAULT_PIXELATION_MULTIPLIER = 10;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public PixelateCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public File applyOperation(File media, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        int pixelationMultiplier = DEFAULT_PIXELATION_MULTIPLIER;

        if (arguments.length > 0) {
            try {
                pixelationMultiplier = Integer.parseInt(arguments[0]);
            } catch (NumberFormatException e) {
                event.getMessage().reply("Pixelation multiplier \"" + arguments[0] + "\" is not a whole number. Using default value of " + pixelationMultiplier + ".").queue();
            }
        }

        return manipulator.pixelate(media, pixelationMultiplier);
    }
}
