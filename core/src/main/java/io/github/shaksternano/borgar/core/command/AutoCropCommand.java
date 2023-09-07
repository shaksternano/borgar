package io.github.shaksternano.borgar.core.command;

import com.google.common.collect.ListMultimap;
import com.sksamuel.scrimage.AutocropOps;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.pixels.PixelsExtractor;
import io.github.shaksternano.borgar.core.command.util.CommandParser;
import io.github.shaksternano.borgar.core.io.NamedFile;
import io.github.shaksternano.borgar.core.media.MediaUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

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
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
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
        var cropColor = rgb < 0 ? null : new Color(rgb);
        return new NamedFile(
            MediaUtil.cropMedia(
                file,
                fileFormat,
                "cropped",
                new CropAreaFinder(cropColor, colorTolerance),
                maxFileSize,
                "Couldn't find area to crop!"
            ),
            "cropped",
            fileFormat
        );
    }

    private static class CropAreaFinder implements Function<BufferedImage, Rectangle> {

        @Nullable
        private Color cropColor;
        private final int colorTolerance;

        private CropAreaFinder(@Nullable Color cropColor, int colorTolerance) {
            this.cropColor = cropColor;
            this.colorTolerance = colorTolerance;
        }

        @Override
        public Rectangle apply(BufferedImage image) {
            var width = image.getWidth();
            var height = image.getHeight();
            PixelsExtractor extractor = rectangle -> ImmutableImage.wrapAwt(image).pixels(
                (int) rectangle.getX(),
                (int) rectangle.getY(),
                (int) rectangle.getWidth(),
                (int) rectangle.getHeight()
            );
            if (cropColor == null) {
                cropColor = new Color(image.getRGB(0, 0), true);
            }
            var x1 = AutocropOps.scanright(cropColor, height, width, 0, extractor, colorTolerance);
            var x2 = AutocropOps.scanleft(cropColor, height, width - 1, extractor, colorTolerance);
            var y1 = AutocropOps.scandown(cropColor, height, width, 0, extractor, colorTolerance);
            var y2 = AutocropOps.scanup(cropColor, width, height - 1, extractor, colorTolerance);
            if (x1 == 0 && y1 == 0 && x2 == width - 1 && y2 == height - 1) {
                return new Rectangle(0, 0, width, height);
            } else {
                return new Rectangle(x1, y1, x2 - x1, y2 - y1);
            }
        }
    }
}
