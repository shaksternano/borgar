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

/**
 * A {@link Command} that manipulates media files.
 */
public abstract class MediaCommand extends Command {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link CommandParser#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public MediaCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Gets a media file using {@link MessageUtil#downloadImage(Message, File)},
     * edits it using {@link #applyOperation(File, String[], MediaManipulator, MessageReceivedEvent)},
     * and then sends it to the channel where the command was triggered.
     *
     * @param arguments The arguments of the command.
     * @param event     The {@link MessageReceivedEvent} that triggered the command.
     */
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
                    editedMedia.deleteOnExit();
                    imageFile.delete();

                    long mediaFileSize = editedMedia.length();
                    if (mediaFileSize > FileUtil.DISCORD_MAXIMUM_FILE_SIZE) {
                        long mediaFileSizeInMb = mediaFileSize / (1024 * 1024);
                        userMessage.reply("The size of the edited media file, " + mediaFileSizeInMb + "MB, is too large to send!").queue();
                        Main.LOGGER.error("File size of edited media was too large to send! (" + mediaFileSize + "B)");
                        editedMedia.delete();
                    } else {
                        userMessage.reply(editedMedia).queue(message -> editedMedia.delete(), throwable -> {
                            editedMedia.delete();
                            String failSend = "Failed to send edited media!";

                            userMessage.reply(failSend).queue();
                            Main.LOGGER.error(failSend, throwable);
                        });
                    }
                } catch (UnsupportedOperationException e) {
                    userMessage.reply("This operation is not supported on files with type \"" + fileExtension + "\"! Reason: " + e.getMessage()).queue();
                } catch (OutOfMemoryError e) {
                    userMessage.reply("The server ran out of memory! Try again later or use a smaller file.").queue();
                    Main.LOGGER.error("Ran out of memory!", e);
                } catch (Throwable t) {
                    String errorMessage = "Error applying operation to media!";

                    userMessage.reply(errorMessage).queue();
                    Main.LOGGER.error(errorMessage, t);
                }
            }, () -> userMessage.reply("Unsupported file type!").queue());
        }, () -> userMessage.reply("No media found!").queue());
    }

    /**
     * Applies an operation to the media file specified by {@link MessageUtil#downloadImage(Message, File)}
     *
     * @param mediaFile   The media file to apply the operation to
     * @param arguments   The arguments of the command.
     * @param manipulator The {@link MediaManipulator} to use for the operation.
     * @param event       The {@link MessageReceivedEvent} that triggered the command.
     * @return The edited media file.
     * @throws IOException If an error occurs while applying the operation.
     */
    public abstract File applyOperation(File mediaFile, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException;
}
