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

public class SpeechBubbleCommand extends MediaCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link CommandParser#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public SpeechBubbleCommand(String name, String description) {
        super(name, description);
    }

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
            media.flush();
            overlay.flush();
            int resizedOverlayHeight = resizedOverlay.getHeight();
            File resizedOverlayFile = FileUtil.getUniqueTempFile(mediaFile.getName() + "_resized_" + overlayName);
            ImageIO.write(resizedOverlay, overlayExtension, resizedOverlayFile);

            File overlaidMediaFile = manipulator.overlayMedia(mediaFile, resizedOverlayFile, 0, -resizedOverlayHeight, true, Color.WHITE, "speech_bubbled");
            resizedOverlayFile.delete();

            return overlaidMediaFile;
        }
    }
}
