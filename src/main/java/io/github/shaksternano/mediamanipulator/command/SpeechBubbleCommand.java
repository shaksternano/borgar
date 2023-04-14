package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.exception.InvalidMediaException;
import io.github.shaksternano.mediamanipulator.image.ImageFrame;
import io.github.shaksternano.mediamanipulator.image.ImageProcessor;
import io.github.shaksternano.mediamanipulator.image.ImageUtil;
import io.github.shaksternano.mediamanipulator.image.OverlayData;
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

    private final boolean cutOut;

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
        this.cutOut = cutOut;
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
    protected File modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
        return MediaUtil.processMedia(
            file,
            fileFormat,
            "speech_bubbled",
            new SpeechBubbleProcessor(cutOut)
        );
    }

    private record SpeechBubbleProcessor(boolean cutOut) implements ImageProcessor<SpeechBubbleData> {

        @Override
        public BufferedImage transformImage(ImageFrame frame, SpeechBubbleData constantData) {
            var image = frame.content();
            var speechBubble = constantData.speechBubble();
            if (cutOut) {
                return ImageUtil.cutoutImage(image, speechBubble, 0, 0, 0xFFFFFF);
            } else {
                return ImageUtil.overlayImage(
                    image,
                    speechBubble,
                    constantData.overlayData(),
                    false,
                    null,
                    null
                );
            }
        }

        /**
         * Returns the speech bubble image.
         */
        @Override
        public SpeechBubbleData constantData(BufferedImage image) throws IOException {
            var speechBubblePath = cutOut
                ? "image/overlay/speech_bubble_2_partial.png"
                : "image/overlay/speech_bubble_1_partial.png";

            var width = image.getWidth();
            var height = image.getHeight();

            var speechBubble = ImageUtil.getImageResourceInRootPackage(speechBubblePath).getFirstImage();

            var minDimension = 3;
            if (width < minDimension) {
                throw new InvalidMediaException("Image width of " + width + " pixels is too small!");
            } else {
                if (speechBubble.getHeight() < speechBubble.getWidth()) {
                    var scaleRatio = (float) width / speechBubble.getWidth();
                    var newHeight = (int) (speechBubble.getHeight() * scaleRatio);

                    if (newHeight < minDimension) {
                        throw new InvalidMediaException("Image height of " + height + " pixels is too small!");
                    }
                }
            }

            BufferedImage resizedSpeechBubble = ImageUtil.fitWidth(speechBubble, width);
            var result = cutOut
                ? resizedSpeechBubble
                : ImageUtil.fill(resizedSpeechBubble, Color.WHITE);
            var overlayData = ImageUtil.getOverlayData(image, result, 0, -result.getHeight(), true, null);
            return new SpeechBubbleData(result, overlayData);
        }
    }

    private record SpeechBubbleData(
        BufferedImage speechBubble,
        OverlayData overlayData
    ) {
    }
}
