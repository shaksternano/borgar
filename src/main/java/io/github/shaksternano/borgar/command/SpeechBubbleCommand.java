package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.exception.InvalidMediaException;
import io.github.shaksternano.borgar.io.FileUtil;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.media.ImageFrame;
import io.github.shaksternano.borgar.media.ImageUtil;
import io.github.shaksternano.borgar.media.MediaUtil;
import io.github.shaksternano.borgar.media.graphics.OverlayData;
import io.github.shaksternano.borgar.media.io.MediaReaders;
import io.github.shaksternano.borgar.media.io.imageprocessor.SingleImageProcessor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Adds a speech bubble on top of media.
 */
public class SpeechBubbleCommand extends FileCommand {

    public static final String FLIP_FLAG = "f";
    public static final String OPAQUE_FLAG = "o";

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
     * @param fileName       The name of the media file.
     * @param fileFormat     The file format of the media file.
     * @param arguments      The arguments of the command.
     * @param extraArguments A multimap mapping the additional parameter names to a list of the arguments.
     * @param event          The {@link MessageReceivedEvent} that triggered the command.
     * @param maxFileSize    The maximum file size of the output file.
     * @return The edited media file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @Override
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        var flipped = extraArguments.containsKey(FLIP_FLAG);
        var opaque = extraArguments.containsKey(OPAQUE_FLAG);
        if (cutOut) {
            fileFormat = MediaUtil.equivalentTransparentFormat(fileFormat);
        }
        return new NamedFile(
            MediaUtil.processMedia(
                file,
                fileFormat,
                "speech_bubbled",
                new SpeechBubbleProcessor(
                    cutOut,
                    flipped,
                    opaque
                ),
                maxFileSize
            ),
            "speech_bubbled",
            fileFormat
        );
    }

    @Override
    public Set<String> parameterNames() {
        return Set.of(
            FLIP_FLAG,
            OPAQUE_FLAG
        );
    }

    private record SpeechBubbleProcessor(
        boolean cutOut,
        boolean flipped,
        boolean opaque
    ) implements SingleImageProcessor<SpeechBubbleData> {

        @Override
        public BufferedImage transformImage(ImageFrame frame, SpeechBubbleData constantData) {
            var image = frame.content();
            var speechBubble = constantData.speechBubble();
            if (cutOut) {
                var cutoutColor = constantData.cutoutColor();
                return ImageUtil.cutoutImage(image, speechBubble, 0, 0, cutoutColor);
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
            var width = image.getWidth();
            var height = image.getHeight();

            var speechBubblePath = cutOut
                ? "media/overlay/speech_bubble_2_partial.png"
                : "media/overlay/speech_bubble_1_partial.png";
            BufferedImage speechBubble;
            try (
                var inputStream = FileUtil.getResourceInRootPackage(speechBubblePath);
                var reader = MediaReaders.createImageReader(inputStream, "png")
            ) {
                speechBubble = reader.first().content();
            }

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

            var resizedSpeechBubble = ImageUtil.fitWidth(speechBubble, width);
            var result = cutOut
                ? resizedSpeechBubble
                : ImageUtil.fill(resizedSpeechBubble, Color.WHITE);
            if (flipped) {
                result = ImageUtil.flipX(result);
            }
            var overlayData = ImageUtil.getOverlayData(image, result, 0, -result.getHeight(), true, null);
            var cutoutColor = opaque ? Color.WHITE.getRGB() : 0;
            return new SpeechBubbleData(
                result,
                overlayData,
                cutoutColor
            );
        }
    }

    private record SpeechBubbleData(
        BufferedImage speechBubble,
        OverlayData overlayData,
        int cutoutColor
    ) {
    }
}
