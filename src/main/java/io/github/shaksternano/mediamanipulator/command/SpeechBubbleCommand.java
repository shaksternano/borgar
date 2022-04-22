package io.github.shaksternano.mediamanipulator.command;

import com.sksamuel.scrimage.ImmutableImage;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Adds a speech bubble on top of media.
 */
public class SpeechBubbleCommand extends MediaCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public SpeechBubbleCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Adds a speech bubble on top of media. The speech bubble is resized so that it's width is the same as the media's width.
     *
     * @param mediaFile   The media file to apply the operation to
     * @param arguments   The arguments of the command.
     * @param manipulator The {@link MediaManipulator} to use for the operation.
     * @param event       The {@link MessageReceivedEvent} that triggered the command.
     * @return The edited media file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public File applyOperation(File mediaFile, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        String overlayExtension = "png";
        String overlayName = "speech_bubble_partial." + overlayExtension;
        String overlayPath = "image/overlay/" + overlayName;

        try (InputStream overlayStream = FileUtil.getResource(overlayPath)) {
            BufferedImage media = ImageIO.read(mediaFile);
            BufferedImage overlay = ImageIO.read(overlayStream);

            BufferedImage resizedOverlay = ImmutableImage.wrapAwt(overlay).scaleToWidth(media.getWidth()).awt();
            BufferedImage fixedOverlay = new BufferedImage(resizedOverlay.getWidth(), resizedOverlay.getHeight(), resizedOverlay.getType());

            Graphics2D graphics = fixedOverlay.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, fixedOverlay.getWidth(), fixedOverlay.getHeight());
            graphics.drawImage(resizedOverlay, 0, 0, null);
            graphics.dispose();
            resizedOverlay.flush();

            media.flush();
            overlay.flush();
            int resizedOverlayHeight = fixedOverlay.getHeight();
            File resizedOverlayFile = FileUtil.getUniqueTempFile(mediaFile.getName() + "_resized_" + overlayName);
            ImageIO.write(fixedOverlay, overlayExtension, resizedOverlayFile);

            File overlaidMediaFile = manipulator.overlayMedia(mediaFile, resizedOverlayFile, 0, -resizedOverlayHeight, true, new Color(0, 0, 0, 0), "speech_bubbled");
            resizedOverlayFile.delete();

            return overlaidMediaFile;
        }
    }
}
