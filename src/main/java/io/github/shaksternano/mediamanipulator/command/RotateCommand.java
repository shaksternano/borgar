package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class RotateCommand extends MediaCommand {

    public static final float DEFAULT_ROTATION = 90F;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public RotateCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public File applyOperation(File media, String fileFormat, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        float rotation = CommandParser.parseFloatArgument(
                arguments,
                0,
                DEFAULT_ROTATION,
                event.getChannel(),
                (argument, defaultValue) -> "Rotation \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );
        int rgb = CommandParser.parseIntegerArgument(
                arguments,
                1,
                -1,
                event.getChannel(),
                (argument, defaultValue) -> "RGB value \"" + argument + "\" is not a whole number. Setting transparent background color."
        );
        return manipulator.rotate(media, fileFormat, rotation, rgb < 0 ? null : new Color(rgb));
    }
}
