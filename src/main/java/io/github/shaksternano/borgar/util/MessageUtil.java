package io.github.shaksternano.borgar.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.shaksternano.borgar.Main;
import io.github.shaksternano.borgar.emoji.EmojiUtil;
import io.github.shaksternano.borgar.io.FileUtil;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.media.ImageUtil;
import io.github.shaksternano.borgar.media.graphics.drawable.Drawable;
import io.github.shaksternano.borgar.media.graphics.drawable.ImageDrawable;
import io.github.shaksternano.borgar.util.tenor.TenorMediaType;
import io.github.shaksternano.borgar.util.tenor.TenorUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contains static methods for dealing with {@link Message}s.
 */
public class MessageUtil {

    /**
     * The maximum number of messages to retrieve from the channel history.
     */
    private static final int MAX_PAST_MESSAGES_TO_CHECK = 50;

    /**
     * Downloads a file.
     *
     * @param message The message to download the file from.
     * @return A {@code CompletableFuture} that will complete with an {@link Optional} describing the file.
     */
    public static CompletableFuture<Optional<NamedFile>> downloadFile(Message message) {
        return processMessagesAsync(message, messageToProcess -> downloadAttachment(messageToProcess)
            .thenCompose(fileOptional -> {
                if (fileOptional.isPresent()) {
                    return CompletableFuture.completedFuture(fileOptional);
                }
                if (!message.equals(messageToProcess)) {
                    var urls = StringUtil.extractUrls(messageToProcess.getContentRaw());
                    if (!urls.isEmpty()) {
                        return FileUtil.downloadFile(urls.get(0))
                            .exceptionallyCompose(throwable -> MessageUtil.downloadEmbedFile(messageToProcess))
                            .thenApply(Optional::of)
                            .exceptionally(throwable -> Optional.empty());
                    }
                }
                return CompletableFuture.completedFuture(Optional.empty());
            })
        );
    }

    public static CompletableFuture<Optional<String>> getUrl(Message message) {
        return processMessages(message, messageToProcess -> {
            var attachments = messageToProcess.getAttachments();
            if (!attachments.isEmpty()) {
                return Optional.of(attachments.get(0).getUrl());
            }
            var urls = StringUtil.extractUrls(messageToProcess.getContentRaw());
            if (!urls.isEmpty()) {
                return Optional.of(urls.get(0));
            }
            for (var embed : messageToProcess.getEmbeds()) {
                var image = embed.getImage();
                if (image != null) {
                    var url = image.getUrl();
                    if (url != null) {
                        return Optional.of(url);
                    }
                }
                var video = embed.getVideoInfo();
                if (video != null) {
                    var url = video.getUrl();
                    if (url != null) {
                        return Optional.of(url);
                    }
                }
            }
            return Optional.empty();
        });
    }

    public static CompletableFuture<Optional<String>> getUrlTenor(Message message) {
        return getUrl(message).thenCompose(urlOptional -> {
            if (urlOptional.isPresent()) {
                var url = urlOptional.orElseThrow();
                try {
                    return TenorUtil.retrieveTenorMediaUrl(url, TenorMediaType.GIF_NORMAL, Main.getTenorApiKey()).thenApply(Optional::of);
                } catch (IllegalArgumentException e) {
                    return CompletableFuture.completedFuture(urlOptional);
                }
            } else {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        });
    }

    /**
     * Processes the message a message is responding to, then the message itself, then previous messages.
     *
     * @param message   The initial message.
     * @param operation The operation to perform on the message the initial message is responding to.
     *                  If this returns an empty {@link Optional}, the operation will be performed on
     *                  the initial message itself,and then if that is also empty, it will be applied
     *                  to previous messages.
     * @param <T>       The type of the result of the operation.
     * @return A {@link CompletableFuture} that will complete with an {@link Optional} describing the result of the operation.
     */
    public static <T> CompletableFuture<Optional<T>> processMessages(
        Message message,
        Function<Message, Optional<T>> operation
    ) {
        return processMessagesAsync(message, operation.andThen(CompletableFuture::completedFuture));
    }

    public static <T> CompletableFuture<Optional<T>> processMessagesAsync(
        Message message,
        Function<Message, CompletableFuture<Optional<T>>> operation
    ) {
        return processMessages(
            message,
            operation,
            MessageUtil::processReferencedMessage,
            MessageUtil::processEmbedLinkedMessage,
            MessageUtil::processMessage,
            MessageUtil::processPreviousMessages
        );
    }

    @SafeVarargs
    private static <T> CompletableFuture<Optional<T>> processMessages(
        Message message,
        Function<Message, CompletableFuture<Optional<T>>> operation,
        BiFunction<Message, Function<Message, CompletableFuture<Optional<T>>>, CompletableFuture<Optional<T>>>... messageProcessors
    ) {
        return CompletableFutureUtil.findFirstAsync(
            Arrays.asList(messageProcessors),
            messageProcessor -> messageProcessor.apply(message, operation)
        );
    }

    private static <T> CompletableFuture<Optional<T>> processReferencedMessage(Message message, Function<Message, CompletableFuture<Optional<T>>> operation) {
        var referencedMessage = message.getReferencedMessage();
        if (referencedMessage == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        } else {
            return operation.apply(referencedMessage);
        }
    }

    private static <T> CompletableFuture<Optional<T>> processEmbedLinkedMessage(Message message, Function<Message, CompletableFuture<Optional<T>>> operation) {
        return getEmbedLinkedMessage(message).thenCompose(linkedMessage -> linkedMessage.map(operation)
            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))
        );
    }

    private static <T> CompletableFuture<Optional<T>> processMessage(Message message, Function<Message, CompletableFuture<Optional<T>>> operation) {
        return operation.apply(message);
    }

    private static <T> CompletableFuture<Optional<T>> processPreviousMessages(Message message, Function<Message, CompletableFuture<Optional<T>>> operation) {
        return message.getChannel()
            .getHistoryBefore(message, MAX_PAST_MESSAGES_TO_CHECK)
            .submit()
            .thenCompose(history -> CompletableFutureUtil.findFirstAsync(
                history.getRetrievedHistory(),
                operation
            ));
    }

    private static CompletableFuture<Optional<NamedFile>> downloadAttachment(Message message) {
        var attachments = message.getAttachments();
        if (attachments.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        var attachment = attachments.get(0);
        var fileName = attachment.getFileName();
        var fileNameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(fileName);
        var fileExtension = FileUtil.getFileExtension(fileName);
        return CompletableFuture.supplyAsync(() -> {
                try {
                    return FileUtil.createTempFile(fileNameWithoutExtension, fileExtension);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).thenCompose(file1 -> attachment.getProxy().downloadToFile(file1))
            .thenApply(file1 -> Optional.of(new NamedFile(file1, fileName)));
    }

    private static CompletableFuture<NamedFile> downloadEmbedFile(Message message) {
        return CompletableFutureUtil.findFirstAsync(
            message.getEmbeds(),
            embed -> {
                var imageInfo = embed.getImage();
                if (imageInfo != null) {
                    var url = imageInfo.getUrl();
                    if (url != null) {
                        return FileUtil.downloadFile(imageInfo.getUrl())
                            .thenApply(Optional::of);
                    }
                }
                var videoInfo = embed.getVideoInfo();
                if (videoInfo != null) {
                    var url = videoInfo.getUrl();
                    if (url != null) {
                        FileUtil.getHeaders(url).thenCompose(headers -> {
                            if (headers.firstValue("Content-Type").orElse("").contains("text")) {
                                return CompletableFuture.completedFuture(Optional.empty());
                            }
                            return FileUtil.downloadFile(videoInfo.getUrl())
                                .thenApply(Optional::of);
                        });
                    }
                }
                return CompletableFuture.completedFuture(Optional.empty());
            }
        ).thenApply(fileOptional -> fileOptional.orElseThrow(() -> new IllegalArgumentException("Message does not contain an embed with media")));
    }

    public static Optional<String> getFirstEmojiUrl(Message message) {
        Map<String, String> emojiUrls = getEmojiUrls(message, true);
        if (emojiUrls.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(emojiUrls.values().iterator().next());
        }
    }

    public static Map<String, String> getEmojiUrls(Message message) {
        return getEmojiUrls(message, false);
    }

    private static Map<String, String> getEmojiUrls(Message message, boolean onlyGetFirst) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        String messageContent = message.getContentRaw();

        // Get custom emojis.
        List<CustomEmoji> customEmojis = message.getMentions().getCustomEmojis();
        for (CustomEmoji customEmoji : customEmojis) {
            builder.put(customEmoji.getAsMention(), customEmoji.getImageUrl());
            if (onlyGetFirst) {
                return builder.buildKeepingLast();
            }
        }

        // Get emojis undetected by Discord.
        if (message.isFromGuild()) {
            Set<String> emoteNames = customEmojis.stream().map(Emoji::getName).collect(ImmutableSet.toImmutableSet());
            for (CustomEmoji customEmoji : message.getGuild().getEmojiCache()) {
                String emoteName = customEmoji.getName();
                if (!emoteNames.contains(emoteName)) {
                    String emoteColonName = ":" + customEmoji.getName() + ":";
                    if (messageContent.contains(emoteColonName)) {
                        builder.put(emoteColonName, customEmoji.getImageUrl());
                        if (onlyGetFirst) {
                            return builder.buildKeepingLast();
                        }
                    }
                }
            }
        }

        // Get unicode emojis.
        int[] codePoints = messageContent.codePoints().toArray();
        for (int i = 0; i < codePoints.length; i++) {
            for (int j = Math.min(codePoints.length - 1, 10 + i); j >= i; j--) {
                List<Integer> compositeCodePoints = new ArrayList<>();
                StringBuilder compositeUnicodeBuilder = new StringBuilder();
                for (int k = i; k <= j; k++) {
                    int codePoint = codePoints[k];
                    String hexCodePoint = Integer.toHexString(codePoint);
                    compositeCodePoints.add(codePoint);
                    compositeUnicodeBuilder.append(hexCodePoint).append("-");
                }
                compositeUnicodeBuilder.deleteCharAt(compositeUnicodeBuilder.length() - 1);

                if (EmojiUtil.isEmojiUnicode(compositeUnicodeBuilder.toString())) {
                    StringBuilder emojiCharactersBuilder = new StringBuilder();
                    for (int codePoint : compositeCodePoints) {
                        emojiCharactersBuilder.appendCodePoint(codePoint);
                    }

                    builder.put(emojiCharactersBuilder.toString(), EmojiUtil.getEmojiUrl(compositeUnicodeBuilder.toString()));

                    if (onlyGetFirst) {
                        return builder.buildKeepingLast();
                    } else {
                        i += j - i;
                        break;
                    }
                }
            }
        }

        // Get unicode emojis from shortcodes.
        StringBuilder emojiNameBuilder = new StringBuilder();
        boolean emojiNameStartDetected = false;
        for (int i = 0; i < messageContent.length(); i++) {
            char character = messageContent.charAt(i);
            if (emojiNameStartDetected) {
                if (character == ':') {
                    String emojiName = emojiNameBuilder.toString();
                    Optional<String> emojiUrlOptional = EmojiUtil.getEmojiUrlFromShortcode(emojiName);
                    if (emojiUrlOptional.isPresent()) {
                        builder.put(':' + emojiName + ':', emojiUrlOptional.orElseThrow());
                        if (onlyGetFirst) {
                            return builder.buildKeepingLast();
                        }
                        emojiNameStartDetected = false;
                    }
                    emojiNameBuilder.setLength(0);
                } else {
                    emojiNameBuilder.append(character);
                }
            } else if (character == ':') {
                emojiNameStartDetected = true;
            }
        }

        return builder.buildKeepingLast();
    }

    /**
     * Gets the linked message in the message embed. For Revolt bridged messages.
     *
     * @param message The message that contains the embed to get the message link from.
     * @return The linked message.
     */
    private static CompletableFuture<Optional<Message>> getEmbedLinkedMessage(Message message) {
        var embeds = message.getEmbeds();
        if (embeds.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        Optional<String> authorUrl = Optional.ofNullable(embeds.get(0).getAuthor())
            .map(MessageEmbed.AuthorInfo::getUrl);
        return getLinkedMessage(authorUrl.orElse(""), message.getChannel());
    }

    private static CompletableFuture<Optional<Message>> getLinkedMessage(String url, MessageChannel channel) {
        try {
            var uri = new URI(url);
            var host = uri.getHost();
            if (host != null && host.contains("discord.com")) {
                var parts = url.split(Pattern.quote("/"));
                if (parts.length >= 1) {
                    try {
                        var messageId = Long.parseLong(parts[parts.length - 1]);
                        return channel.retrieveMessageById(messageId).submit().thenApply(Optional::of);
                    } catch (NumberFormatException ignored) {
                    } catch (RuntimeException e) {
                        Main.getLogger().error("Error getting linked message!", e);
                    }
                }
            }
        } catch (URISyntaxException e) {
            Main.getLogger().error("Failed to parse URL " + url + "!", e);
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    public static Map<String, Drawable> getEmojiImages(Message message) {
        var emojiUrls = MessageUtil.getEmojiUrls(message);
        Map<String, String> mediaUrls = new HashMap<>(emojiUrls);
        var urls = StringUtil.extractUrls(message.getContentRaw());
        for (var url : urls) {
            mediaUrls.put(url, url);
        }
        return mediaUrls.entrySet().parallelStream().map(imageUrlEntry -> {
                var emojiCode = imageUrlEntry.getKey();
                var emojiImageUrl = imageUrlEntry.getValue();
                InputStream mediaStream = null;
                var needsClosing = true;
                try {
                    var url = new URL(emojiImageUrl);
                    try (
                        var formatStream = FileUtil.readUrl(url)
                    ) {
                        String format;
                        try {
                            format = ImageUtil.getImageFormat(formatStream);
                        } catch (Exception e) {
                            format = FileUtil.getFileExtension(emojiImageUrl);
                        }
                        mediaStream = FileUtil.readUrl(url);
                        var emoji = new ImageDrawable(mediaStream, format);
                        needsClosing = false;
                        return Optional.of(Map.entry(emojiCode, emoji));
                    }
                } catch (IOException e) {
                    return Optional.<Map.Entry<String, Drawable>>empty();
                } finally {
                    if (needsClosing && mediaStream != null) {
                        try {
                            mediaStream.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }).flatMap(Optional::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static String enlargeImageUrl(String url) {
        return url + "?size=1024";
    }
}
