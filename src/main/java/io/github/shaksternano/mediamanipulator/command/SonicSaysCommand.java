package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.mediamanipulator.util.MediaManipulatorRegistry;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SonicSaysCommand extends BaseCommand {

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) throws IOException {
        Message userMessage = event.getMessage();

        Map<String, Drawable> nonTextParts = MessageUtil.getNonTextParts(userMessage);
        MediaManipulator manipulator = MediaManipulatorRegistry.getManipulator("jpg").orElseThrow();
        File sonicSays = manipulator.sonicSays(arguments, nonTextParts);
        File compressedMedia = manipulator.compress(sonicSays, "jpg");

        long mediaFileSize = compressedMedia.length();
        if (mediaFileSize > FileUtil.DISCORD_MAXIMUM_FILE_SIZE) {
            long mediaFileSizeInMb = mediaFileSize / (1024 * 1024);
            userMessage.reply("The size of the edited media file, " + mediaFileSizeInMb + "MB, is too large to send!").queue();
            Main.getLogger().error("File size of edited media was too large to send! (" + mediaFileSize + "B)");
            sonicSays.delete();
            compressedMedia.delete();
        } else {
            userMessage.reply(compressedMedia).queue(message -> {
                sonicSays.delete();
                compressedMedia.delete();
            }, throwable -> {
                sonicSays.delete();
                compressedMedia.delete();
                String failSend = "Failed to send edited media!";
                userMessage.reply(failSend).queue();
                Main.getLogger().error(failSend, throwable);
            });
        }
    }
}
