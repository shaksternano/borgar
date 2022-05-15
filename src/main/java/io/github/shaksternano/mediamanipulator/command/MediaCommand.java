package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.command.util.InvalidMediaException;
import io.github.shaksternano.mediamanipulator.command.util.MissingArgumentException;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.mediamanipulator.util.MediaManipulatorRegistry;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A {@link Command} that manipulates media files.
 */
public abstract class MediaCommand extends BaseCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public MediaCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Gets a media file using {@link FileUtil#downloadFile(String, String)},
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

        MessageUtil.downloadImage(userMessage, tempDirectory.toString()).ifPresentOrElse(file -> {
            String fileType = FileUtil.getFileType(file);

            MediaManipulatorRegistry.getManipulator(fileType).ifPresentOrElse(manipulator -> {
                try {
                    File editedMedia = applyOperation(file, arguments, manipulator, event);
                    editedMedia.deleteOnExit();
                    file.delete();

                    long mediaFileSize = editedMedia.length();
                    if (mediaFileSize > FileUtil.DISCORD_MAXIMUM_FILE_SIZE) {
                        long mediaFileSizeInMb = mediaFileSize / (1024 * 1024);
                        userMessage.reply("The size of the edited media file, " + mediaFileSizeInMb + "MB, is too large to send!").queue();
                        Main.getLogger().error("File size of edited media was too large to send! (" + mediaFileSize + "B)");
                        editedMedia.delete();
                    } else {
                        userMessage.reply(editedMedia).queue(message -> editedMedia.delete(), throwable -> {
                            editedMedia.delete();
                            String failSend = "Failed to send edited media!";

                            userMessage.reply(failSend).queue();
                            Main.getLogger().error(failSend, throwable);
                        });
                    }
                } catch (InvalidMediaException e) {
                    userMessage.reply(e.getMessage() == null ? "Invalid media!" : "Invalid media: " + e.getMessage()).queue();
                    Main.getLogger().error("Invalid media!", e);
                } catch (UnsupportedOperationException e) {
                    String unsupportedMessage = "This operation is not supported on files with type \"" + fileType + "\"!";

                    if (e.getMessage() != null) {
                        unsupportedMessage = unsupportedMessage + " Reason: " + e.getMessage();
                    }

                    userMessage.reply(unsupportedMessage).queue();
                    Main.getLogger().warn("Unsupported operation!", e);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } catch (OutOfMemoryError e) {
                    userMessage.reply("The server ran out of memory! Try again later or use a smaller file.").queue();
                    Main.getLogger().error("Ran out of memory executing command " + getName() + "!", e);
                }
            }, () -> userMessage.reply("Unsupported file type!").queue());
        }, () -> userMessage.reply("No media found!").queue());
    }

    /**
     * Applies an operation to the media file specified by {@link FileUtil#downloadFile(String, String)}.
     *
     * @param media       The media file to apply the operation to
     * @param arguments   The arguments of the command.
     * @param manipulator The {@link MediaManipulator} to use for the operation.
     * @param event       The {@link MessageReceivedEvent} that triggered the command.
     * @return The edited media file.
     * @throws IOException              If an error occurs while applying the operation.
     * @throws IllegalArgumentException If an argument is invalid.
     * @throws MissingArgumentException If the operation requires an argument but none was provided.
     */
    public abstract File applyOperation(File media, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException;
}
