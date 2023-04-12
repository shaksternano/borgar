package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.exception.InvalidMediaException;
import io.github.shaksternano.mediamanipulator.exception.UnsupportedFileFormatException;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
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
import java.util.concurrent.CompletableFuture;

public abstract class FileCommand extends BaseCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public FileCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public void execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws Exception {
        var triggerMessage = event.getMessage();
        MessageUtil.downloadFile(triggerMessage, FileUtil.getTempDir().toString()).ifPresentOrElse(file -> {
            var fileFormat = FileUtil.getFileFormat(file);
            File editedMedia = null;
            File compressedMedia = null;
            try {
                editedMedia = modifyFile(file, fileFormat, arguments, extraArguments, event);
                var newFileFormat = FileUtil.getFileFormat(editedMedia);
                compressedMedia = compress(editedMedia, newFileFormat, event.getGuild());
                var mediaFileSize = compressedMedia.length();
                if (mediaFileSize > DiscordUtil.getMaxUploadSize(event.getGuild())) {
                    handleTooLargeFile(mediaFileSize, triggerMessage);
                } else {
                    tryReply(triggerMessage, compressedMedia);
                }
            } catch (Throwable t) {
                handleError(t, triggerMessage, fileFormat);
            } finally {
                deleteAll(file, editedMedia, compressedMedia);
            }
        }, () -> triggerMessage.reply("No media found!").queue());
    }

    private static File compress(File file, String fileFormat, Guild guild) {
        return MediaManipulatorRegistry.getManipulator(fileFormat)
            .map(manipulator -> {
                try {
                    return manipulator.compress(file, fileFormat, guild);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
            .orElse(file);
    }

    private static void handleTooLargeFile(long fileSize, Message triggerMessage) {
        var mediaFileSizeInMb = fileSize / MiscUtil.TO_MB;
        triggerMessage.reply("The size of the edited media file, " + mediaFileSizeInMb + "MB, is too large to send!").queue();
        Main.getLogger().error("File size of edited media was too large to send! (" + fileSize + "MB)");
    }

    private static void tryReply(Message triggerMessage, File file) {
        MiscUtil.repeatTry(
            () -> reply(triggerMessage, file),
            3,
            5,
            FileCommand::handleReplyAttemptFailure,
            maxAttempts -> handleReplyFailure(maxAttempts, triggerMessage)
        );
    }

    private static CompletableFuture<?> reply(Message triggerMessage, File file) {
        return triggerMessage.replyFiles(FileUpload.fromData(file)).submit();
    }

    private static void handleReplyAttemptFailure(int attempts, Throwable error) {
        Main.getLogger().error(attempts + " failed attempt" + (attempts == 1 ? "" : "s") + " to send edited media!", error);
    }

    private static void handleReplyFailure(int totalAttempts, Message triggerMessage) {
        triggerMessage.reply("Failed to send edited media, please try again!").queue();
        Main.getLogger().error("Failed to send edited media in " + totalAttempts + " attempts!");
    }

    private void handleError(Throwable error, Message triggerMessage, String fileFormat) {
        if (error instanceof InvalidMediaException) {
            triggerMessage.reply(error.getMessage() == null ? "Invalid media!" : "Invalid media: " + error.getMessage()).queue();
            Main.getLogger().error("Invalid media!", error);
        } else if (error instanceof UnsupportedFileFormatException) {
            var unsupportedMessage = "This operation is not supported on files with type \"" + fileFormat + "\"!";
            if (error.getMessage() != null && !error.getMessage().isBlank()) {
                unsupportedMessage = unsupportedMessage + " Reason: " + error.getMessage();
            }
            triggerMessage.reply(unsupportedMessage).queue();
        } else if (error instanceof IOException e) {
            throw new UncheckedIOException(e);
        } else if (error instanceof OutOfMemoryError) {
            triggerMessage.reply("The server ran out of memory! Try again later or use a smaller file.").queue();
            Main.getLogger().error("Ran out of memory executing command " + getNameWithPrefix() + "!", error);
        } else if (error instanceof RuntimeException e) {
            throw e;
        } else if (error instanceof Error e) {
            throw e;
        } else {
            throw new RuntimeException(error);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteAll(File... files) {
        for (var file : files) {
            if (file != null) {
                file.delete();
            }
        }
    }

    public abstract File modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException;
}
