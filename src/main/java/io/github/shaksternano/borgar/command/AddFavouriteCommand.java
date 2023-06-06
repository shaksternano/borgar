package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.command.util.CommandResponse;
import io.github.shaksternano.borgar.data.repository.SavedFileRepository;
import io.github.shaksternano.borgar.io.FileUtil;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.media.ImageUtil;
import io.github.shaksternano.borgar.media.MediaUtil;
import io.github.shaksternano.borgar.media.io.MediaReaders;
import io.github.shaksternano.borgar.media.io.imageprocessor.BasicImageProcessor;
import io.github.shaksternano.borgar.media.io.reader.LimitedDurationMediaReader;
import io.github.shaksternano.borgar.media.io.reader.NoAudioReader;
import io.github.shaksternano.borgar.util.DiscordUtil;
import io.github.shaksternano.borgar.util.MessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AddFavouriteCommand extends BaseCommand<AddFavouriteCommand.ResponseData> {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public AddFavouriteCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public CompletableFuture<CommandResponse<ResponseData>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        return MessageUtil.getFileUrl(event.getMessage()).thenCompose(aliasUrlOptional ->
            aliasUrlOptional.map(url -> getOrCreateAliasGif(url, event))
                .orElseGet(() -> new CommandResponse<ResponseData>("No media found!").asFuture())
        );
    }

    @Override
    public void handleFirstResponse(Message response, MessageReceivedEvent event, @Nullable ResponseData responseData) {
        var errorMessage = "Error linking alias gif!";
        if (responseData == null) {
            return;
        }
        FileUtil.delete(responseData.output());
        var attachments = response.getAttachments();
        if (attachments.isEmpty()) {
            if (responseData.createdNewAlias()) {
                event.getMessage().reply(errorMessage).queue();
            }
            return;
        }
        var url = responseData.url();
        var aliasUrl = attachments.get(0).getProxyUrl();
        SavedFileRepository.addAliasFuture(url, aliasUrl);
    }

    private static CompletableFuture<CommandResponse<ResponseData>> getOrCreateAliasGif(String url, MessageReceivedEvent event) {
        return SavedFileRepository.findAliasUrlFuture(url).thenApply(fileUrlOptional -> {
            if (fileUrlOptional.isPresent()) {
                return new CommandResponse<>(fileUrlOptional.orElseThrow());
            }
            File input = null;
            try {
                var namedFile = FileUtil.downloadFile(url);
                input = namedFile.file();
                var fileFormat = FileUtil.getFileFormat(namedFile.file());
                var maxFileSize = DiscordUtil.getMaxUploadSize(event);
                var aliasGif = createAliasGif(namedFile, fileFormat, maxFileSize);
                return new CommandResponse<ResponseData>(aliasGif.file(), aliasGif.name())
                    .withResponseData(new ResponseData(aliasGif.file(), url, true));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                FileUtil.delete(input);
            }
        });
    }

    private static NamedFile createAliasGif(NamedFile input, String fileFormat, long maxFileSize) throws IOException {
        var imageReader = MediaReaders.createImageReader(input.file(), fileFormat);
        var audioReader = NoAudioReader.INSTANCE;
        var resultName = "favourite_" + input.nameWithoutExtension();
        var outputFormat = "gif";
        return new NamedFile(
            MediaUtil.processMedia(
                new LimitedDurationMediaReader<>(imageReader, 5_000_000),
                audioReader,
                outputFormat,
                resultName,
                new BasicImageProcessor(image -> ImageUtil.bound(image, 200)),
                maxFileSize
            ),
            resultName,
            outputFormat
        );
    }

    public record ResponseData(
        File output,
        String url,
        boolean createdNewAlias
    ) {
    }
}
