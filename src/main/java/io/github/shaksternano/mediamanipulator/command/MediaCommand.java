package io.github.shaksternano.mediamanipulator.command;

import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulatorRegistry;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;

public abstract class MediaCommand extends Command {

    public MediaCommand(String name, String description) {
        super(name, description);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        File tempDirectory = FileUtil.getTempDirectory();

        MessageUtil.downloadImage(userMessage, tempDirectory).ifPresentOrElse(imageFile -> {
            String fileExtension = Files.getFileExtension(imageFile.getName());

            MediaManipulatorRegistry.getManipulator(fileExtension).ifPresentOrElse(manipulator -> {
                try {
                    File editedMedia = applyOperation(imageFile, arguments, manipulator, event);
                    imageFile.delete();
                    editedMedia.deleteOnExit();
                    userMessage.reply(editedMedia).queue(message -> {
                        editedMedia.delete();
                    }, throwable -> {
                        editedMedia.delete();
                        String failSend = "Failed to send edited media!";
                        userMessage.reply(failSend).queue();
                        Main.LOGGER.error(failSend, throwable);
                    });
                } catch (IOException e) {
                    String errorMessage = "Error applying operation to media!";
                    userMessage.reply(errorMessage).queue();
                    Main.LOGGER.error(errorMessage, e);
                } catch (UnsupportedOperationException e) {
                    userMessage.reply("This operation is not supported on this file type!").queue();
                }
            }, () -> userMessage.reply("Unsupported file type!").queue());
        }, () -> userMessage.reply("No media found!").queue());
    }

    public abstract File applyOperation(File mediaFile, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException;
}
