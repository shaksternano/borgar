package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.exception.MissingArgumentException;
import io.github.shaksternano.mediamanipulator.image.FrameData;
import io.github.shaksternano.mediamanipulator.image.ImageProcessor;
import io.github.shaksternano.mediamanipulator.io.MediaUtil;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class CropCommand extends FileCommand {

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
    public File modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
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
            return MediaUtil.processMedia(
                    file,
                    fileFormat,
                    "cropped",
                    new CropProcessor(topRatio, rightRatio, bottomRatio, leftRatio)
            );
        }
    }

    private record CropProcessor(float topRatio, float rightRatio, float bottomRatio, float leftRatio) implements ImageProcessor<CropData> {

        @Override
        public BufferedImage transformImage(BufferedImage image, FrameData frameData, CropData globalData) {
            return image.getSubimage(globalData.x(), globalData.y(), globalData.width(), globalData.height());
        }

        @Override
        public CropData globalData(BufferedImage image) {
            int width = image.getWidth();
            int height = image.getHeight();

            int x = Math.min((int) (width * leftRatio), width - 1);
            int y = Math.min((int) (height * topRatio), height - 1);
            int newWidth = Math.max((int) (width * (1 - leftRatio - rightRatio)), 1);
            int newHeight = Math.max((int) (height * (1 - topRatio - bottomRatio)), 1);

            return new CropData(x, y, newWidth, newHeight);
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

    private record CropData(int x, int y, int width, int height) {}
}
