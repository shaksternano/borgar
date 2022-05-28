package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.mediamanipulator.util.MediaManipulatorRegistry;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SonicSaysCommand extends OptionalFileInputMediaCommand {

    public static final String IMAGE_PATH = "image/background/sonic_says.jpg";
    public static final String IMAGE_NAME = "sonic_says.jpg";
    public static final int SPEECH_BUBBLE_X = 345;
    public static final int SPEECH_BUBBLE_Y = 35;
    public static final int SPEECH_BUBBLE_WIDTH = 630;
    public static final int SPEECH_BUBBLE_HEIGHT = 490;
    public static final int SPEECH_BUBBLE_PADDING = 50;
    public static final int DOUBLE_SPEECH_PADDING = SPEECH_BUBBLE_PADDING * 2;


    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public SonicSaysCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public File applyOperation(File media, String fileFormat, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        return manipulator.sonicSaysImage(media, fileFormat);
    }

    @Override
    public File applyOperation(String[] arguments, MessageReceivedEvent event) throws IOException {
        Map<String, Drawable> nonTextParts = MessageUtil.getNonTextParts(event.getMessage());
        MediaManipulator manipulator = MediaManipulatorRegistry.getManipulator("jpg").orElseThrow();
        return manipulator.sonicSaysText(arguments, nonTextParts);
    }
}
