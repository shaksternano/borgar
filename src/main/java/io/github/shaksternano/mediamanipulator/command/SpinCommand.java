package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;

public class SpinCommand extends MediaCommand {

    public static final int DEFAULT_SPIN_SPEED = 1;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public SpinCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public File applyOperation(File media, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        float spinSpeed = DEFAULT_SPIN_SPEED;

        if (arguments.length > 0) {
            try {
                spinSpeed = Float.parseFloat(arguments[0]);
            } catch (NumberFormatException e) {
                event.getMessage().reply("Spin speed \"" + arguments[0] + "\" is not a number. Using default value of " + spinSpeed + ".").queue();
            }
        }

        return manipulator.spin(media, spinSpeed);
    }
}
