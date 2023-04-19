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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract sealed class BaseFileCommand extends BaseCommand permits FileCommand, OptionalFileInputFileCommand {

    private final boolean requireFileInput;

    /**
     * Creates a new command object.
     *
     * @param name             The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                         followed by this name, the command will be executed.
     * @param description      The description of the command. This is displayed in the help command.
     * @param requireFileInput Whether the command requires a file input.
     */
    public BaseFileCommand(String name, String description, boolean requireFileInput) {
        super(name, description);
        this.requireFileInput = requireFileInput;
    }

    protected abstract File modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException;

    protected abstract File createFile(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException;

    @Override
    public void execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        File input = null;
        File edited = null;
        File compressed = null;
        var fileFormat = "N/A";
        var triggerMessage = event.getMessage();
        try {
            Optional<File> fileOptional = requireFileInput || arguments.isEmpty()
                ? MessageUtil.downloadFile(triggerMessage, FileUtil.getTempDir().toString())
                : Optional.empty();
            if (requireFileInput && fileOptional.isEmpty()) {
                triggerMessage.reply("No media found!").queue();
            } else {
                input = fileOptional.orElse(null);
                fileFormat = fileOptional.map(FileUtil::getFileFormat).orElse(fileFormat);
                var finalFileFormat = fileFormat;
                edited = fileOptional.map(file -> {
                    try {
                        return modifyFile(file, finalFileFormat, arguments, extraArguments, event);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }).orElseGet(() -> {
                    try {
                        return createFile(arguments, extraArguments, event);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
                var newFileFormat = FileUtil.getFileFormat(edited);
                compressed = compress(edited, newFileFormat, event.getGuild());
                var resultSize = compressed.length();
                if (resultSize > DiscordUtil.getMaxUploadSize(event.getGuild())) {
                    handleTooLargeFile(resultSize, triggerMessage);
                } else {
                    tryReply(triggerMessage, compressed);
                }
            }
        } catch (InvalidMediaException e) {
            triggerMessage.reply(e.getMessage() == null ? "Invalid media!" : "Invalid media: " + e.getMessage()).queue();
            Main.getLogger().error("Invalid media!", e);
        } catch (UnsupportedFileFormatException e) {
            var unsupportedMessage = "This operation is not supported on files with type \"" + fileFormat + "\"!";
            if (e.getMessage() != null && !e.getMessage().isBlank()) {
                unsupportedMessage = unsupportedMessage + " Reason: " + e.getMessage();
            }
            triggerMessage.reply(unsupportedMessage).queue();
        } catch (OutOfMemoryError e) {
            triggerMessage.reply("The server ran out of memory! Try again later or use a smaller file.").queue();
            Main.getLogger().error("Ran out of memory executing command " + getNameWithPrefix() + "!", e);
        } finally {
            deleteAll(input, edited, compressed);
        }
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
            BaseFileCommand::handleReplyAttemptFailure,
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteAll(File... files) {
        for (var file : files) {
            if (file != null) {
                file.delete();
            }
        }
    }
}
