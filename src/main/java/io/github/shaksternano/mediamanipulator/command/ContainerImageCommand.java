package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.image.backgroundimage.ContainerImageInfo;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.mediamanipulator.util.MediaManipulatorRegistry;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ContainerImageCommand extends OptionalFileInputMediaCommand {

    private final ContainerImageInfo CONTAINER_IMAGE_INFO;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ContainerImageCommand(String name, String description, ContainerImageInfo containerImageInfo) {
        super(name, description);
        this.CONTAINER_IMAGE_INFO = containerImageInfo;
    }

    @Override
    public File applyOperation(File media, String fileFormat, List<String> arguments, Multimap<String, String> extraArguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        return manipulator.containerImageWithImage(media, fileFormat, CONTAINER_IMAGE_INFO);
    }

    @Override
    public File applyOperation(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
        Map<String, Drawable> nonTextParts = MessageUtil.getNonTextParts(event.getMessage());
        MediaManipulator manipulator = MediaManipulatorRegistry.getManipulator("png").orElseThrow();
        return manipulator.containerImageWithText(arguments, nonTextParts, CONTAINER_IMAGE_INFO);
    }
}
