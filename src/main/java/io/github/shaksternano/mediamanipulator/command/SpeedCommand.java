package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.media.ImageFrame;
import io.github.shaksternano.mediamanipulator.media.MediaUtil;
import io.github.shaksternano.mediamanipulator.media.io.Imageprocessor.SingleImageProcessor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
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
    protected File modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
        var speedMultiplier = CommandParser.parseFloatArgument(
            arguments,
            0,
            DEFAULT_SPEED_MULTIPLIER,
            null,
            event.getChannel(),
            (argument, defaultValue) -> "Speed multiplier \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );
        var processor = new SpeedProcessor(speedMultiplier);
        return MediaUtil.processMedia(file, fileFormat, "changed_speed", processor);
    }

    private record SpeedProcessor(float speed) implements SingleImageProcessor<Boolean> {

        @Override
        public BufferedImage transformImage(ImageFrame frame, Boolean constantData) {
            return frame.content();
        }

        @Override
        public Boolean constantData(BufferedImage image) {
            return true;
        }

        @Override
        public float speed() {
            return speed;
        }
    }
}
