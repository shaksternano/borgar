package io.github.shaksternano.mediamanipulator.util;

import com.google.common.collect.ImmutableList;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Contains static methods for dealing with {@link Message}s.
 */
public class MessageUtil {

    /**
     * The maximum number of messages to retrieve from the channel history.
     */
    private static final int MAX_PAST_MESSAGES_TO_CHECK = 50;

    /**
     * Downloads an image.
     *
     * @param message   The message to download the image from.
     * @param directory The directory to download the image to.
     * @return An {@link Optional} describing the image file.
     */
    public static Optional<File> downloadImage(Message message, String directory) {
        return processMessages(message, messageToProcess -> {
            Optional<File> imageFileOptional = downloadAttachmentImage(messageToProcess, directory);
            if (imageFileOptional.isPresent()) {
                return imageFileOptional;
            } else {
                List<String> urls = StringUtil.extractUrls(messageToProcess.getContentRaw());
                if (!urls.isEmpty()) {
                    imageFileOptional = FileUtil.downloadFile(urls.get(0), directory);
                    if (imageFileOptional.isPresent()) {
                        return imageFileOptional;
                    } else {
                        imageFileOptional = downloadEmbedImage(messageToProcess, directory);
                        if (imageFileOptional.isPresent()) {
                            return imageFileOptional;
                        }
                    }
                }
            }

            return Optional.empty();
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
     * @return An {@link Optional} describing the result of the operation.
     */
    public static <T> Optional<T> processMessages(Message message, Function<Message, Optional<T>> operation) {
        Optional<T> result;

        Message referencedMessage = message.getReferencedMessage();
        if (referencedMessage != null) {
            result = operation.apply(referencedMessage);
            if (result.isPresent()) {
                return result;
            }
        }

        Optional<Message> linkedMessage = getEmbedLinkedMessage(message);
        if (linkedMessage.isPresent()) {
            result = operation.apply(linkedMessage.orElseThrow());
            if (result.isPresent()) {
                return result;
            }
        }

        result = operation.apply(message);
        if (result.isPresent()) {
            return result;
        }

        List<Message> previousMessages = getPreviousMessages(message.getChannel(), MAX_PAST_MESSAGES_TO_CHECK);
        for (Message previousMessage : previousMessages) {
            result = operation.apply(previousMessage);
            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }

    /**
     * Gets the previous messages in the channel.
     *
     * @param channel The channel to get the previous messages from.
     * @param amount  The amount of messages to get.
     * @return A list of messages. If an error occurred, an empty list is returned.
     */
    public static List<Message> getPreviousMessages(MessageChannel channel, int amount) {
        try {
            return channel.getHistory().retrievePast(amount).complete();
        } catch (RuntimeException e) {
            Main.getLogger().error("Error while retrieving previous messages", e);
        }

        return ImmutableList.of();
    }

    /**
     * Downloads an image from an attachment.
     *
     * @param message   The message to download the image from.
     * @param directory The directory to download the image to.
     * @return An {@link Optional} describing the image file.
     */
    private static Optional<File> downloadAttachmentImage(Message message, String directory) {
        List<Message.Attachment> attachments = message.getAttachments();

        for (Message.Attachment attachment : attachments) {
            if (attachment.isImage()) {
                File imageFile = FileUtil.getUniqueFile(directory, attachment.getFileName());

                try {
                    return Optional.of(attachment.downloadToFile(imageFile).get(10, TimeUnit.SECONDS));
                } catch (ExecutionException | InterruptedException e) {
                    Main.getLogger().error("Error downloading image!", e);
                } catch (TimeoutException e) {
                    Main.getLogger().error("Image took too long to download!", e);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Downloads an image file from an embed.
     *
     * @param message   The message containing the embed to download the image from.
     * @param directory The directory to download the image to.
     * @return An {@link Optional} describing the image file.
     */
    private static Optional<File> downloadEmbedImage(Message message, String directory) {
        List<MessageEmbed> embeds = message.getEmbeds();

        for (MessageEmbed embed : embeds) {
            MessageEmbed.ImageInfo imageInfo = embed.getImage();

            if (imageInfo != null) {
                return FileUtil.downloadFile(imageInfo.getUrl(), directory);
            }
        }

        return Optional.empty();
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

    public static Map<String, String> getEmojiUrls(Message message, boolean onlyGetFirst) {
        Map<String, String> emojiUrls = new HashMap<>();
        List<Emote> emotes = message.getEmotes();

        for (Emote emote : emotes) {
            emojiUrls.put(emote.getAsMention(), emote.getImageUrl());

            if (onlyGetFirst) {
                return emojiUrls;
            }
        }

        String messageContent = message.getContentRaw();
        int[] codePoints = messageContent.codePoints().toArray();
        for (int codePoint : codePoints) {
            String hexCodePoint = Integer.toHexString(codePoint);
            if (hexCodePoint.length() >= 5) {
                String emojiUrl = "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/" + hexCodePoint + ".png";

                try {
                    BufferedImage emojiImage = ImageUtil.readImage(new URL(emojiUrl));

                    if (emojiImage == null) {
                        Main.getLogger().error("Could not read image from URL " + emojiUrl + "!");
                    } else {
                        emojiImage.flush();
                        String emojiCharacters = String.valueOf(Character.toChars(codePoint));
                        emojiUrls.put(emojiCharacters, emojiUrl);

                        if (onlyGetFirst) {
                            return emojiUrls;
                        }
                    }
                } catch (MalformedURLException e) {
                    Main.getLogger().error("Failed to parse emoji URL " + emojiUrl + "!", e);
                } catch (IOException ignored) {
                }
            }
        }

        return emojiUrls;
    }

    /**
     * Gets the linked message in the message embed. For Revolt bridged messages.
     *
     * @param message The message that contains the embed to get the message link from.
     * @return The linked message.
     */
    private static Optional<Message> getEmbedLinkedMessage(Message message) {
        List<MessageEmbed> embeds = message.getEmbeds();

        for (MessageEmbed embed : embeds) {
            Optional<String> authorUrl = Optional.ofNullable(embed.getAuthor()).map(MessageEmbed.AuthorInfo::getUrl);
            return getLinkedMessage(authorUrl.orElse(""), message.getChannel());
        }

        return Optional.empty();
    }

    private static Optional<Message> getLinkedMessage(String url, MessageChannel channel) {
        try {
            URI uri = new URI(url);

            if (uri.getHost().contains("discord.com")) {
                String[] parts = url.split(Pattern.quote("/"));

                if (parts.length >= 1) {
                    try {
                        long messageId = Long.parseLong(parts[parts.length - 1]);
                        return Optional.of(channel.retrieveMessageById(messageId).complete());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (URISyntaxException e) {
            Main.getLogger().error("Failed to parse URL " + url + "!", e);
        }

        return Optional.empty();
    }
}
