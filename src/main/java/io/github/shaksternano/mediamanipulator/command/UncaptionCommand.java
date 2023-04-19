package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.media.ImageUtil;
import io.github.shaksternano.mediamanipulator.media.MediaUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class UncaptionCommand extends FileCommand {

    private final boolean coloredCaption;

    /**
     * Creates a new command object.
     *
     * @param name           The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                       followed by this name, the command will be executed.
     * @param description    The description of the command. This is displayed in the help command.
     * @param coloredCaption If the caption contains color, for example emojis.
     */
    public UncaptionCommand(String name, String description, boolean coloredCaption) {
        super(name, description);
        this.coloredCaption = coloredCaption;
    }

    @Override
    protected File modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
        return MediaUtil.cropMedia(
            file,
            fileFormat,
            "uncaptioned",
            this::findNonCaptionAreaTopAndBottom
        );
    }

    private Rectangle findNonCaptionAreaTopAndBottom(BufferedImage image) {
        Rectangle nonCaptionArea = new Rectangle(0, 0, image.getWidth(), image.getHeight());

        Rectangle nonTopCaptionArea = coloredCaption ?
            findNonCaptionAreaColored(image, true) :
            findNonCaptionArea(image, true);
        nonCaptionArea = nonCaptionArea.intersection(nonTopCaptionArea);

        Rectangle nonBottomCaptionArea = coloredCaption ?
            findNonCaptionAreaColored(image, false) :
            findNonCaptionArea(image, false);
        nonCaptionArea = nonCaptionArea.intersection(nonBottomCaptionArea);

        return nonCaptionArea;
    }

    private static Rectangle findNonCaptionArea(BufferedImage image, boolean topCaption) {
        boolean continueLooking = true;
        int captionEnd = -1;
        int y = topCaption ? 0 : image.getHeight() - 1;
        while (topCaption ? y < image.getHeight() : y >= 0) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                if (!ImageUtil.isGreyScale(color)) {
                    continueLooking = false;
                    break;
                }
            }

            if (continueLooking) {
                captionEnd = y;

                if (topCaption) {
                    y++;
                } else {
                    y--;
                }
            } else {
                break;
            }
        }

        return createNonCaptionArea(image, topCaption, captionEnd);
    }

    private static Rectangle findNonCaptionAreaColored(BufferedImage image, boolean topCaption) {
        boolean continueLooking = true;
        int captionEnd = -1;
        int y = topCaption ? 0 : image.getHeight() - 1;
        int colorTolerance = 150;
        while (topCaption ? y < image.getHeight() : y >= 0) {
            boolean rowIsCompletelyWhite = true;
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                double colorDistance = ImageUtil.colorDistance(color, Color.WHITE);
                if (colorDistance > colorTolerance) {
                    rowIsCompletelyWhite = false;
                    if ((topCaption ? y == 0 : y == image.getHeight() - 1)
                        || x == 0
                        || x == image.getWidth() - 1
                    ) {
                        continueLooking = false;
                        break;
                    }
                } else if (rowIsCompletelyWhite && x == image.getWidth() - 1) {
                    captionEnd = y;
                }
            }

            if (continueLooking) {
                if (topCaption) {
                    y++;
                } else {
                    y--;
                }
            } else {
                break;
            }
        }

        return createNonCaptionArea(image, topCaption, captionEnd);
    }

    private static Rectangle createNonCaptionArea(BufferedImage image, boolean topCaption, int captionEnd) {
        if (captionEnd != -1) {
            int width = image.getWidth();
            int height = topCaption ? image.getHeight() - captionEnd - 1 : captionEnd;

            if (width > 0 && height > 0) {
                if (topCaption) {
                    return new Rectangle(0, captionEnd + 1, width, height);
                } else {
                    return new Rectangle(0, 0, width, height);
                }
            }
        }

        return new Rectangle(0, 0, image.getWidth(), image.getHeight());
    }
}
