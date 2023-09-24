package io.github.shaksternano.borgar.core.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.core.Main;
import io.github.shaksternano.borgar.core.command.util.CommandResponse;
import io.github.shaksternano.borgar.core.data.repository.SavedUrlRepository;
import io.github.shaksternano.borgar.core.io.FileUtil;
import io.github.shaksternano.borgar.core.io.NamedFile;
import io.github.shaksternano.borgar.core.media.ImageFrameOld;
import io.github.shaksternano.borgar.core.media.ImageUtil;
import io.github.shaksternano.borgar.core.media.MediaReaders;
import io.github.shaksternano.borgar.core.media.MediaUtil;
import io.github.shaksternano.borgar.core.media.graphics.GraphicsUtil;
import io.github.shaksternano.borgar.core.media.graphics.drawable.Drawable;
import io.github.shaksternano.borgar.core.media.graphics.drawable.TextDrawable;
import io.github.shaksternano.borgar.core.media.imageprocessor.SingleImageProcessor;
import io.github.shaksternano.borgar.core.media.readerold.LimitedDurationMediaReader;
import io.github.shaksternano.borgar.core.media.readerold.NoAudioReader;
import io.github.shaksternano.borgar.core.util.DiscordUtil;
import io.github.shaksternano.borgar.core.util.MessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AddFavouriteCommand extends BaseCommand<AddFavouriteCommand.ResponseData> {

    public static String ALIAS_PREFIX = "favourite_";

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
        return MessageUtil.getUrl(event.getMessage()).thenCompose(aliasUrlOptional ->
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
        SavedUrlRepository.createAliasFuture(url, aliasUrl).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                Main.getLogger().error("Error linking alias gif", throwable);
            }
        });
    }

    private static CompletableFuture<CommandResponse<ResponseData>> getOrCreateAliasGif(
        String url,
        MessageReceivedEvent event
    ) {
        var fileExtension = FileUtil.getFileExtension(url);
        if (fileExtension.equals("gif")) {
            return new CommandResponse<ResponseData>("This is already a GIF file!").asFuture();
        }

        if (fileExtension.equals("png") || fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
            return FileUtil.downloadFile(url).thenCompose(namedFile -> {
                try {
                    var renamed = FileUtil.changeFileExtension(
                        namedFile.getFile(),
                        namedFile.getName(),
                        "gif"
                    );
                    return new CommandResponse<ResponseData>(renamed).asFuture();
                } catch (IOException e) {
                    return CompletableFuture.failedFuture(e);
                }
            });
        }

        return SavedUrlRepository.readAliasUrlFuture(url)
            .thenCompose(aliasUrlOptional -> aliasUrlOptional.map(aliasUrl ->
                new CommandResponse<ResponseData>(aliasUrl).asFuture()
            ).orElseGet(() -> FileUtil.downloadFile(url).thenCompose(namedFile -> {
                File input = null;
                try {
                    input = namedFile.getFile();
                    var fileFormat = FileUtil.getFileFormat(namedFile.getFile());
                    var maxFileSize = DiscordUtil.getMaxUploadSize(event);
                    var aliasGif = createAliasGif(namedFile, url, fileFormat, maxFileSize, event);
                    return new CommandResponse<ResponseData>(aliasGif)
                        .withResponseData(new ResponseData(aliasGif.getFile(), url, true))
                        .asFuture();
                } catch (IOException e) {
                    return CompletableFuture.failedFuture(e);
                } finally {
                    FileUtil.delete(input);
                }
            })));
    }

    private static NamedFile createAliasGif(
        NamedFile input,
        String originalUrl,
        String fileFormat,
        long maxFileSize,
        MessageReceivedEvent event
    ) throws IOException {
        var imageReader = MediaReaders.createImageReader(input.getFile(), fileFormat);
        var audioReader = NoAudioReader.INSTANCE;
        var encodedUrl = Base64.getEncoder().encodeToString(originalUrl.getBytes());
        var resultName = ALIAS_PREFIX + encodedUrl;
        var outputFormat = "gif";
        return new NamedFile(
            MediaUtil.processMedia(
                new LimitedDurationMediaReader<>(imageReader, 5_000_000),
                audioReader,
                outputFormat,
                resultName,
                new AddFavouriteProcessor(fileFormat, event),
                maxFileSize
            ),
            resultName,
            outputFormat
        );
    }

    private record AddFavouriteProcessor(
        String fileFormat,
        MessageReceivedEvent event
    ) implements SingleImageProcessor<AddFavouriteData> {

        private static final Color TEXT_BOX_COLOR = new Color(0, 0, 0, 150);
        private static final Color TEXT_COLOR = Color.WHITE;

        @Override
        public BufferedImage transformImage(ImageFrameOld frame, AddFavouriteData constantData) throws IOException {
            var resized = resizeImage(frame.getContent());
            var graphics = resized.createGraphics();
            ImageUtil.configureTextDrawQuality(graphics);
            var padding = constantData.padding();
            graphics.drawImage(constantData.icon(), padding, padding, null);
            graphics.setColor(TEXT_BOX_COLOR);
            graphics.fillRoundRect(
                constantData.textBoxX(),
                padding,
                constantData.textBoxWidth(),
                constantData.textBoxHeight(),
                constantData.cornerRadius(),
                constantData.cornerRadius()
            );
            graphics.setFont(constantData.font());
            graphics.setColor(TEXT_COLOR);
            constantData.formatText.draw(
                graphics,
                constantData.textX(),
                constantData.textY(),
                frame.getTimestamp()
            );
            return resized;
        }

        @Override
        public AddFavouriteData constantData(BufferedImage image) throws IOException {
            var iconUrl = event.getJDA().getSelfUser().getEffectiveAvatarUrl();
            try (
                var inputStream = new URL(iconUrl).openStream();
                var reader = MediaReaders.createImageReader(inputStream, "png")
            ) {
                var resized = resizeImage(image);
                var imageWidth = resized.getWidth();
                var imageHeight = resized.getHeight();
                var smallestDimension = Math.min(imageWidth, imageHeight);
                var padding = (int) (smallestDimension * 0.05);

                var icon = reader.first().getContent();
                var iconTargetWidth = (int) (smallestDimension * 0.2);
                var resizedIcon = ImageUtil.fitWidth(icon, iconTargetWidth);
                var iconWidth = resizedIcon.getWidth();
                var iconHeight = resizedIcon.getHeight();
                var iconSmallestDimension = Math.min(iconWidth, iconHeight);
                var cornerRadius = (iconSmallestDimension * 0.2F);
                var roundedCorners = ImageUtil.makeRoundedCorners(resizedIcon, cornerRadius);

                var graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
                ImageUtil.configureTextDrawQuality(graphics);

                var formatText = new TextDrawable(fileFormat.toUpperCase());
                var font = new Font("Helvetica Neue", Font.PLAIN, smallestDimension);
                graphics.setFont(font);
                var textBoxPadding = (int) (iconSmallestDimension * 0.1);
                var textBoxMaxWidth = 2 * iconWidth - 2 * textBoxPadding;
                var textBoxMaxHeight = iconHeight - 2 * textBoxPadding;

                GraphicsUtil.fontFitWidth(textBoxMaxWidth, formatText, graphics);
                GraphicsUtil.fontFitHeight(textBoxMaxHeight, formatText, graphics);
                var resizedFont = graphics.getFont();

                var textBoxWidth = formatText.getWidth(graphics) + 2 * textBoxPadding;
                var textBoxHeight = formatText.getHeight(graphics) + 2 * textBoxPadding;
                var textBoxX = imageWidth - padding - textBoxWidth;

                var textX = textBoxX + textBoxPadding;
                var textY = padding + textBoxPadding;

                graphics.dispose();

                return new AddFavouriteData(
                    roundedCorners,
                    (int) cornerRadius,
                    padding,
                    formatText,
                    resizedFont,
                    textBoxX,
                    textBoxWidth,
                    textBoxHeight,
                    textBoxPadding,
                    textX,
                    textY
                );
            }
        }

        private static BufferedImage resizeImage(BufferedImage image) {
            return ImageUtil.bound(image, 300);
        }
    }

    private record AddFavouriteData(
        BufferedImage icon,
        int cornerRadius,
        int padding,
        Drawable formatText,
        Font font,
        int textBoxX,
        int textBoxWidth,
        int textBoxHeight,
        int textBoxPadding,
        int textX,
        int textY
    ) {
    }

    public record ResponseData(
        File output,
        String url,
        boolean createdNewAlias
    ) {
    }
}
