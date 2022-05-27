package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.graphics.drawable.ImageDrawable;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A command that adds a captions media.
 */
public class CaptionCommand extends MediaCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public CaptionCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Adds a caption to media, with the caption text being the arguments of the command joined together with spaces in between.
     *
     * @param media       The media file to apply the operation to.
     * @param fileFormat  The file format of the media file.
     * @param arguments   The arguments of the command.
     * @param manipulator The {@link MediaManipulator} to use for the operation.
     * @param event       The {@link MessageReceivedEvent} that triggered the command.
     * @return The edited media file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @Override
    public File applyOperation(File media, String fileFormat, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        Map<String, String> imageUrls = MessageUtil.getEmojiUrls(event.getMessage());
        Map<String, Drawable> nonTextParts = imageUrls.entrySet().parallelStream().map(imageUrlEntry -> {
            try {
                BufferedImage image = ImageIO.read(new URL(imageUrlEntry.getValue()));
                Drawable drawable = new ImageDrawable(image);
                return new AbstractMap.SimpleEntry<>(imageUrlEntry.getKey(), drawable);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));

        return manipulator.caption(media, fileFormat, arguments, nonTextParts);
    }
}
