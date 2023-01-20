package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.exception.InvalidMediaException;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.io.ImageProcessor;
import io.github.shaksternano.mediamanipulator.io.MediaUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Adds a speech bubble on top of media.
 */
public class SpeechBubbleCommand extends FileCommand {

    private final boolean CUT_OUT;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     * @param cutOut      Whether the speech bubble should be cut out of the media.
     */
    public SpeechBubbleCommand(String name, String description, boolean cutOut) {
        super(name, description);
        CUT_OUT = cutOut;
    }

    /**
     * Adds a speech bubble on top of media. The speech bubble is resized so that it's width is the same as the media's width.
     *
     * @param file           The media file to apply the operation to.
     * @param fileFormat     The file format of the media file.
     * @param arguments      The arguments of the command.
     * @param extraArguments A multimap mapping the additional parameter names to a list of the arguments.
     * @param event          The {@link MessageReceivedEvent} that triggered the command.
     * @return The edited media file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @Override
    public File modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
        return MediaUtil.processMedia(
            file,
            fileFormat,
            "speech_bubbled",
            new SpeechBubbleProcessor(CUT_OUT)
        );
    }

    private record SpeechBubbleProcessor(boolean cutOut) implements ImageProcessor<BufferedImage> {

        @Override
        public BufferedImage transformImage(BufferedImage image, BufferedImage extraData) {
            if (cutOut) {
                return ImageUtil.cutoutImage(image, extraData, 0, 0, 0xFFFFFF);
            } else {
                return ImageUtil.overlayImage(image, extraData, false, 0, -extraData.getHeight(), null, null, null, true);
            }
        }

        /**
         * Returns the speech bubble image.
         */
        @Override
        public BufferedImage globalData(BufferedImage image) throws IOException {
            String speechBubblePath = cutOut ? "image/overlay/speech_bubble_2_partial.png"
                : "image/overlay/speech_bubble_1_partial.png";

            int width = image.getWidth();
            int height = image.getHeight();

            BufferedImage speechBubble = ImageUtil.getImageResourceInRootPackage(speechBubblePath).getFirstImage();

            int minDimension = 3;
            if (width < minDimension) {
                throw new InvalidMediaException("Image width of " + width + " pixels is too small!");
            } else {
                if (speechBubble.getHeight() < speechBubble.getWidth()) {
                    float scaleRatio = (float) width / speechBubble.getWidth();
                    int newHeight = (int) (speechBubble.getHeight() * scaleRatio);

                    if (newHeight < minDimension) {
                        throw new InvalidMediaException("Image height of " + height + " pixels is too small!");
                    }
                }
            }

            BufferedImage resizedSpeechBubble = ImageUtil.fitWidth(speechBubble, width);

            if (cutOut) {
                return resizedSpeechBubble;
            } else {
                return ImageUtil.fill(resizedSpeechBubble, Color.WHITE);
            }
        }
    }
}
