package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.exception.InvalidMediaException;
import io.github.shaksternano.mediamanipulator.exception.MissingArgumentException;
import io.github.shaksternano.mediamanipulator.exception.UnsupportedFileFormatException;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.mediamanipulator.util.MediaManipulatorRegistry;
import io.github.shaksternano.mediamanipulator.util.DiscordUtil;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import io.github.shaksternano.mediamanipulator.util.MiscUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
     * edits it using {@link #applyOperation(File, String, List, ListMultimap, MediaManipulator, MessageReceivedEvent)},
     * and then sends it to the channel where the command was triggered.
     *
     * @param arguments      The arguments of the command.
     * @param extraArguments A multimap mapping the additional parameter names to a list of the arguments.
     * @param event          The {@link MessageReceivedEvent} that triggered the command.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        MessageUtil.downloadFile(userMessage, FileUtil.getTempDir().toString()).join().ifPresentOrElse(file -> {
            String fileFormat = FileUtil.getFileFormat(file);

            MediaManipulatorRegistry.getManipulator(fileFormat).ifPresentOrElse(manipulator -> {
                File editedMedia = null;
                File compressedMedia = null;

                try {
                    editedMedia = applyOperation(file, fileFormat, arguments, extraArguments, manipulator, event);
                    String newFileFormat = FileUtil.getFileFormat(editedMedia);
                    Optional<MediaManipulator> manipulatorOptional = MediaManipulatorRegistry.getManipulator(newFileFormat);
                    Guild guild = event.isFromGuild() ? event.getGuild() : null;
                    if (manipulatorOptional.isPresent()) {
                        compressedMedia = manipulatorOptional.orElseThrow().compress(editedMedia, newFileFormat, guild);
                    } else {
                        compressedMedia = editedMedia;
                    }

                    long mediaFileSize = compressedMedia.length();
                    if (mediaFileSize > DiscordUtil.getMaxUploadSize(guild)) {
                        long mediaFileSizeInMb = mediaFileSize / MiscUtil.TO_MB;
                        userMessage.reply("The size of the edited media file, " + mediaFileSizeInMb + "MB, is too large to send!").queue();
                        Main.getLogger().error("File size of edited media was too large to send! (" + mediaFileSize + "MB)");
                    } else {
                        boolean success = false;
                        int maxAttempts = 3;
                        for (int attempts = 0; attempts < maxAttempts; attempts++) {
                            try {
                                userMessage.replyFiles(FileUpload.fromData(compressedMedia)).complete();
                                success = true;
                                break;
                            } catch (RuntimeException e) {
                                Main.getLogger().error((attempts + 1) + " failed attempt" + (attempts == 0 ? "" : "s") + " to send edited media!", e);

                                if (attempts < maxAttempts - 1) {
                                    try {
                                        TimeUnit.SECONDS.sleep(1);
                                    } catch (InterruptedException e2) {
                                        Main.getLogger().error("Interrupted while waiting to send again!!", e2);
                                    }
                                }
                            }
                        }

                        if (!success) {
                            userMessage.reply("Failed to send edited media, please try again!").queue();
                            Main.getLogger().error("Failed to send edited media in " + maxAttempts + " attempts!");
                        }
                    }
                } catch (InvalidMediaException e) {
                    userMessage.reply(e.getMessage() == null ? "Invalid media!" : "Invalid media: " + e.getMessage()).queue();
                    Main.getLogger().error("Invalid media!", e);
                } catch (UnsupportedFileFormatException e) {
                    String unsupportedMessage = "This operation is not supported on files with type \"" + fileFormat + "\"!";

                    if (e.getMessage() != null && !e.getMessage().isBlank()) {
                        unsupportedMessage = unsupportedMessage + " Reason: " + e.getMessage();
                    }

                    userMessage.reply(unsupportedMessage).queue();
                    Main.getLogger().warn("Unsupported operation!", e);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } catch (OutOfMemoryError e) {
                    userMessage.reply("The server ran out of memory! Try again later or use a smaller file.").queue();
                    Main.getLogger().error("Ran out of memory executing command " + getNameWithPrefix() + "!", e);
                } finally {
                    file.delete();
                    if (editedMedia != null) {
                        editedMedia.delete();
                    }
                    if (compressedMedia != null) {
                        compressedMedia.delete();
                    }
                }
            }, () -> userMessage.reply("Unsupported file type!").queue());
        }, () -> userMessage.reply("No media found!").queue());
    }

    /**
     * Applies an operation to the media file specified by {@link FileUtil#downloadFile(String, String)}.
     *
     * @param media          The media file to apply the operation to
     * @param fileFormat     The type of the media file.
     * @param arguments      The arguments of the command.
     * @param extraArguments A multimap mapping the additional parameter names to a list of the arguments.
     * @param manipulator    The {@link MediaManipulator} to use for the operation.
     * @param event          The {@link MessageReceivedEvent} that triggered the command.
     * @return The edited media file.
     * @throws IOException              If an error occurs while applying the operation.
     * @throws IllegalArgumentException If an argument is invalid.
     * @throws MissingArgumentException If the operation requires an argument but none was provided.
     */
    public abstract File applyOperation(File media, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException;
}
