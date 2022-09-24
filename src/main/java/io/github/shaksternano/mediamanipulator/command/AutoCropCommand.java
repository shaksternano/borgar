package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AutoCropCommand extends MediaCommand {

    public static final int DEFAULT_COLOR_TOLERANCE = 50;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public AutoCropCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public File applyOperation(File media, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        int rgb = CommandParser.parseIntegerArgument(
                arguments,
                0,
                -1,
                null,
                event.getChannel(),
                (argument, defaultValue) -> "RGB value \"" + argument + "\" is not a whole number, choosing transparent color."
        );
        int colorTolerance = CommandParser.parseIntegerArgument(
                arguments,
                1,
                DEFAULT_COLOR_TOLERANCE,
                integer -> integer >= 0 && integer <= 255,
                event.getChannel(),
                (argument, defaultValue) -> "Color tolerance \"" + argument + "\" is not a whole number, choosing default value of " + defaultValue + "."
        );
        return manipulator.autoCrop(media, fileFormat, rgb < 0 ? new Color(0, 0, 0, 0) : new Color(rgb), colorTolerance);
    }
}
