package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.command.util.CommandParser;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.media.MediaUtil;
import io.github.shaksternano.borgar.media.io.imageprocessor.SpeedProcessor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SpeedCommand extends FileCommand {

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
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        var speedMultiplier = CommandParser.parseFloatArgument(
            arguments,
            0,
            DEFAULT_SPEED_MULTIPLIER,
            null,
            event.getChannel(),
            (argument, defaultValue) -> "Speed multiplier \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );
        return new NamedFile(
            MediaUtil.processMedia(
                file,
                fileFormat,
                "changed_speed",
                new SpeedProcessor(speedMultiplier),
                maxFileSize
            ),
            "changed_speed",
            fileFormat
        );
    }
}
