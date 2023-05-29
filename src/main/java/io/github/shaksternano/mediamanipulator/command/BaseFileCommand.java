package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.exception.InvalidMediaException;
import io.github.shaksternano.mediamanipulator.exception.UnsupportedFileFormatException;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.io.NamedFile;
import io.github.shaksternano.mediamanipulator.mediamanipulator.util.MediaManipulatorRegistry;
import io.github.shaksternano.mediamanipulator.util.DiscordUtil;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.entities.Guild;
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

    protected abstract NamedFile modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException;

    protected abstract NamedFile createFile(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException;

    @Override
    public CompletableFuture<List<MessageCreateData>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        var triggerMessage = event.getMessage();
        CompletableFuture<Optional<NamedFile>> fileOptionalFuture = requireFileInput || arguments.isEmpty()
            ? MessageUtil.downloadFile(triggerMessage)
            : CompletableFuture.completedFuture(Optional.empty());
        return fileOptionalFuture.thenApply(namedFileOptional -> {
            File input = null;
            File edited = null;
            File compressed = null;
            var fileFormat = "N/A";
            try {
                if (requireFileInput && namedFileOptional.isEmpty()) {
                    return MessageUtil.createResponse("No media found!");
                } else {
                    var fileOptional = namedFileOptional.map(NamedFile::file);
                    input = fileOptional.orElse(null);
                    fileFormat = fileOptional.map(FileUtil::getFileFormat).orElse(fileFormat);
                    var finalFileFormat = fileFormat;
                    var namedEdited = fileOptional.map(file -> {
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
                    edited = namedEdited.file();
                    var newFileFormat = FileUtil.getFileFormat(edited);
                    compressed = compress(edited, newFileFormat, event.getGuild());
                    var resultSize = compressed.length();
                    if (resultSize > DiscordUtil.getMaxUploadSize(event.getGuild())) {
                        return MessageUtil.createResponse(
                            "The size of the edited media file, " + resultSize + "MB, is too large to send!"
                        );
                    } else {
                        return List.of(MessageCreateData.fromFiles(
                            FileUpload.fromData(compressed, namedEdited.name())
                        ));
                    }
                }
            } catch (InvalidMediaException e) {
                return MessageUtil.createResponse(
                    e.getMessage() == null ? "Invalid media!" : "Invalid media: " + e.getMessage()
                );
            } catch (UnsupportedFileFormatException e) {
                var unsupportedMessage = "This operation is not supported on files with type \"" + fileFormat + "\"!";
                if (e.getMessage() != null && !e.getMessage().isBlank()) {
                    unsupportedMessage = unsupportedMessage + " Reason: " + e.getMessage();
                }
                return MessageUtil.createResponse(unsupportedMessage);
            } catch (OutOfMemoryError e) {
                Main.getLogger().error("Ran out of memory executing command " + getNameWithPrefix() + "!", e);
                return MessageUtil.createResponse(
                    "The server ran out of memory! Try again later or use a smaller file."
                );
            } finally {
                deleteAll(input, edited, compressed);
            }
        });
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteAll(File... files) {
        for (var file : files) {
            if (file != null) {
                file.delete();
            }
        }
    }
}
