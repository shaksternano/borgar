package io.github.shaksternano.borgar.core.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.core.Main;
import io.github.shaksternano.borgar.core.command.util.CommandResponse;
import io.github.shaksternano.borgar.core.exception.InvalidMediaException;
import io.github.shaksternano.borgar.core.exception.UnreadableFileException;
import io.github.shaksternano.borgar.core.exception.UnsupportedFileFormatException;
import io.github.shaksternano.borgar.core.io.FileUtil;
import io.github.shaksternano.borgar.core.io.NamedFile;
import io.github.shaksternano.borgar.core.util.DiscordUtil;
import io.github.shaksternano.borgar.core.util.MessageUtil;
import io.github.shaksternano.borgar.core.util.MiscUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract sealed class BaseFileCommand extends BaseCommand<List<File>> permits FileCommand, OptionalFileInputFileCommand {

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

    protected abstract NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException;

    protected abstract NamedFile createFile(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException;

    @Override
    public CompletableFuture<CommandResponse<List<File>>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        CompletableFuture<Optional<NamedFile>> fileOptionalFuture = requireFileInput || arguments.isEmpty()
            ? MessageUtil.downloadFile(event.getMessage())
            : CompletableFuture.completedFuture(Optional.empty());
        return fileOptionalFuture.thenApply(namedFileOptional -> {
            File input = null;
            File output = null;
            var fileFormat = "N/A";
            try {
                if (requireFileInput && namedFileOptional.isEmpty()) {
                    return new CommandResponse<>("No media found!");
                } else {
                    var fileOptional = namedFileOptional.map(NamedFile::getFile);
                    input = fileOptional.orElse(null);
                    fileFormat = fileOptional.map(FileUtil::getFileFormat).orElse(fileFormat);
                    var finalFileFormat = fileFormat;
                    var maxFileSize = DiscordUtil.getMaxUploadSize(event);
                    var namedEdited = namedFileOptional.map(namedFile -> {
                        var file = namedFile.getFile();
                        var fileName = namedFile.getName();
                        try {
                            return modifyFile(file, fileName, finalFileFormat, arguments, extraArguments, event, maxFileSize);
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
                    output = namedEdited.getFile();
                    var outputSize = output.length();
                    if (outputSize > DiscordUtil.getMaxUploadSize(event)) {
                        var outputSizeMb = outputSize / MiscUtil.TO_MB;
                        Main.getLogger().error("The size of the edited media file, " + outputSizeMb + "MB, is too large to send");
                        return new CommandResponse<>(
                            "The size of the edited media file, " + outputSizeMb + "MB, is too large to send!"
                        );
                    } else {
                        List<File> toDelete = new ArrayList<>();
                        toDelete.add(input);
                        toDelete.add(output);
                        var response = new CommandResponse<List<File>>(namedEdited)
                            .withResponseData(toDelete);
                        // Don't delete the input and output files yet. They will be deleted after the response is sent.
                        input = null;
                        output = null;
                        return response;
                    }
                }
            } catch (InvalidMediaException e) {
                return new CommandResponse<>(
                    e.getMessage() == null ? "Invalid media!" : "Invalid media: " + e.getMessage()
                );
            } catch (UnsupportedFileFormatException e) {
                String unsupportedMessage;
                if (e.getMessage() != null && !e.getMessage().isBlank()) {
                    unsupportedMessage = e.getMessage();
                } else {
                    unsupportedMessage = "This operation is not supported on files with type `" + fileFormat + "`!";
                }
                return new CommandResponse<>(unsupportedMessage);
            } catch (UncheckedIOException e) {
                if (e.getCause() instanceof UnreadableFileException) {
                    Main.getLogger().error("Could not read file", e);
                    return new CommandResponse<>("Could not read file!");
                }
                throw e;
            } catch (OutOfMemoryError e) {
                Main.getLogger().error("Ran out of memory executing command " + nameWithPrefix() + "!", e);
                return new CommandResponse<>(
                    "The server ran out of memory! Try using a smaller file."
                );
            } finally {
                FileUtil.delete(input, output);
            }
        });
    }

    @Override
    public void handleFirstResponse(Message response, MessageReceivedEvent event, @Nullable List<File> responseData) {
        if (responseData != null) {
            FileUtil.delete(responseData);
        }
    }
}
