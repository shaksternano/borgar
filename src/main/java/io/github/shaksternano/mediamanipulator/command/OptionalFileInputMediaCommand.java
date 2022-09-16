package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.exception.InvalidMediaException;
import io.github.shaksternano.mediamanipulator.exception.UnsupportedFileFormatException;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.mediamanipulator.util.MediaManipulatorRegistry;
import io.github.shaksternano.mediamanipulator.util.DiscordUtil;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import io.github.shaksternano.mediamanipulator.util.MiscUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class OptionalFileInputMediaCommand extends BaseCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public OptionalFileInputMediaCommand(String name, String description) {
        super(name, description);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();

        File file = arguments.size() == 0 ? MessageUtil.downloadFile(userMessage, FileUtil.getTempDir().toString()).orElse(null) : null;
        String fileFormat = file == null ? null : FileUtil.getFileFormat(file);
        MediaManipulator manipulator = fileFormat == null ? null : MediaManipulatorRegistry.getManipulator(fileFormat).orElse(null);

        File editedMedia = null;
        File compressedMedia = null;

        try {
            if (file == null) {
                editedMedia = applyOperation(arguments, extraArguments, event);
            } else {
                editedMedia = applyOperation(file, fileFormat, arguments, extraArguments, manipulator, event);
            }

            String newFileFormat = FileUtil.getFileFormat(editedMedia);
            Optional<MediaManipulator> manipulatorOptional = MediaManipulatorRegistry.getManipulator(newFileFormat);
            if (manipulatorOptional.isPresent()) {
                compressedMedia = manipulatorOptional.orElseThrow().compress(editedMedia, newFileFormat, event.getGuild());
            } else {
                compressedMedia = editedMedia;
            }

            long mediaFileSize = compressedMedia.length();
            if (mediaFileSize > DiscordUtil.getMaxUploadSize(event.getGuild())) {
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
            if (file != null) {
                file.delete();
            }
            if (editedMedia != null) {
                editedMedia.delete();
            }
            if (compressedMedia != null) {
                compressedMedia.delete();
            }
        }
    }

    public abstract File applyOperation(File media, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException;

    public abstract File applyOperation(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException;
}
