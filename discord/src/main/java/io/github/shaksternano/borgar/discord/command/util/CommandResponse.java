package io.github.shaksternano.borgar.discord.command.util;

import io.github.shaksternano.borgar.discord.io.NamedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record CommandResponse<T>(List<MessageCreateData> responses, boolean suppressEmbeds, @Nullable T responseData) {

    public CommandResponse(String message) {
        this(MessageCreateData.fromContent(message));
    }

    public CommandResponse(File file, String fileName) {
        this(MessageCreateData.fromFiles(FileUpload.fromStreamSupplier(fileName, () -> {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
        })));
    }

    public CommandResponse(NamedFile file) {
        this(file.file(), file.name());
    }

    public CommandResponse(InputStream inputStream, String fileName) {
        this(MessageCreateData.fromFiles(FileUpload.fromData(inputStream, fileName)));
    }

    public CommandResponse(MessageCreateData message) {
        this(List.of(message));
    }

    public CommandResponse(List<MessageCreateData> responses) {
        this(responses, false, null);
    }

    public CommandResponse<T> withSuppressEmbeds(boolean suppressEmbeds) {
        return new CommandResponse<>(responses, suppressEmbeds, responseData);
    }

    public CommandResponse<T> withResponseData(T responseData) {
        return new CommandResponse<>(responses, false, responseData);
    }

    public CompletableFuture<CommandResponse<T>> asFuture() {
        return CompletableFuture.completedFuture(this);
    }
}
