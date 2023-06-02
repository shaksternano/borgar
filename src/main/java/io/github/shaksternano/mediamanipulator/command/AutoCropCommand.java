package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import com.sksamuel.scrimage.AutocropOps;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.pixels.PixelsExtractor;
import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.io.NamedFile;
import io.github.shaksternano.mediamanipulator.media.MediaUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AutoCropCommand extends FileCommand {

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
    protected NamedFile modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
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
        Color cropColor = rgb < 0 ? new Color(0, 0, 0, 0) : new Color(rgb);
        return new NamedFile(
            MediaUtil.cropMedia(
                file,
                fileFormat,
                "cropped",
                image -> findAutoCropArea(image, cropColor, colorTolerance),
                maxFileSize
            ),
            "cropped",
            fileFormat
        );
    }

    private static Rectangle findAutoCropArea(BufferedImage image, Color cropColor, int colorTolerance) {
        int width = image.getWidth();
        int height = image.getHeight();
        PixelsExtractor extractor = rectangle -> ImmutableImage.wrapAwt(image).pixels(
            (int) rectangle.getX(),
            (int) rectangle.getY(),
            (int) rectangle.getWidth(),
            (int) rectangle.getHeight()
        );

        int x1 = AutocropOps.scanright(cropColor, height, width, 0, extractor, colorTolerance);
        int x2 = AutocropOps.scanleft(cropColor, height, width - 1, extractor, colorTolerance);
        int y1 = AutocropOps.scandown(cropColor, height, width, 0, extractor, colorTolerance);
        int y2 = AutocropOps.scanup(cropColor, width, height - 1, extractor, colorTolerance);
        if (x1 == 0 && y1 == 0 && x2 == width - 1 && y2 == height - 1) {
            return new Rectangle(0, 0, width, height);
        } else {
            return new Rectangle(x1, y1, x2 - x1, y2 - y1);
        }
    }
}
