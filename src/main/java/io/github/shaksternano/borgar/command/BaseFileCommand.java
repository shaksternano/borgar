package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.Main;
import io.github.shaksternano.borgar.exception.InvalidMediaException;
import io.github.shaksternano.borgar.exception.UnsupportedFileFormatException;
import io.github.shaksternano.borgar.io.FileUtil;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.util.DiscordUtil;
import io.github.shaksternano.borgar.util.MessageUtil;
import io.github.shaksternano.borgar.util.MiscUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

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

    protected abstract NamedFile modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException;

    protected abstract NamedFile createFile(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException;

    @Override
    public CompletableFuture<List<MessageCreateData>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        CompletableFuture<Optional<NamedFile>> fileOptionalFuture = requireFileInput || arguments.isEmpty()
            ? MessageUtil.downloadFile(event.getMessage())
            : CompletableFuture.completedFuture(Optional.empty());
        return fileOptionalFuture.thenApply(namedFileOptional -> {
            File input = null;
            File output = null;
            var fileFormat = "N/A";
            try {
                if (requireFileInput && namedFileOptional.isEmpty()) {
                    return MessageUtil.createResponse("No media found!");
                } else {
                    var fileOptional = namedFileOptional.map(NamedFile::file);
                    input = fileOptional.orElse(null);
                    fileFormat = fileOptional.map(FileUtil::getFileFormat).orElse(fileFormat);
                    var finalFileFormat = fileFormat;
                    var maxFileSize = DiscordUtil.getMaxUploadSize(event);
                    var namedEdited = fileOptional.map(file -> {
                        try {
                            return modifyFile(file, finalFileFormat, arguments, extraArguments, event, maxFileSize);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }).orElseGet(() -> {
                        try {
                            return createFile(arguments, extraArguments, event, maxFileSize);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                    output = namedEdited.file();
                    var outputSize = output.length();
                    if (outputSize > DiscordUtil.getMaxUploadSize(event)) {
                        var outputSizeMb = outputSize / MiscUtil.TO_MB;
                        Main.getLogger().error("The size of the edited media file, " + outputSizeMb + "MB, is too large to send");
                        return MessageUtil.createResponse(
                            "The size of the edited media file, " + outputSizeMb + "MB, is too large to send!"
                        );
                    } else {
                        return List.of(MessageCreateData.fromFiles(
                            FileUpload.fromData(output, namedEdited.name())
                        ));
                    }
                }
            } catch (InvalidMediaException e) {
                return MessageUtil.createResponse(
                    e.getMessage() == null ? "Invalid media!" : "Invalid media: " + e.getMessage()
                );
            } catch (UnsupportedFileFormatException e) {
                String unsupportedMessage;
                if (e.getMessage() != null && !e.getMessage().isBlank()) {
                    unsupportedMessage = e.getMessage();
                } else {
                    unsupportedMessage = "This operation is not supported on files with type `" + fileFormat + "`!";
                }
                return MessageUtil.createResponse(unsupportedMessage);
            } catch (OutOfMemoryError e) {
                Main.getLogger().error("Ran out of memory executing command " + getNameWithPrefix() + "!", e);
                return MessageUtil.createResponse(
                    "The server ran out of memory! Try using a smaller file."
                );
            } finally {
                deleteAll(input, output);
            }
        });
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
