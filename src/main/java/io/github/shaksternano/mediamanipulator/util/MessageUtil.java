package io.github.shaksternano.mediamanipulator.util;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    /**
     * Downloads an image.
     * @param message
     * @return
     */
    public static Optional<File> downloadImage(Message message, File directory) {
        return downloadImage(message, directory, true);
    }

    private static Optional<File> downloadImage(Message message, File directory, boolean checkReplies) {
        Optional<File> imageFileOptional = downloadAttachmentImage(message, directory);
        if (imageFileOptional.isPresent()) {
            return imageFileOptional;
        }

        imageFileOptional = downloadUrlImage(message.getContentRaw(), directory, true);
        if (imageFileOptional.isPresent()) {
            return imageFileOptional;
        }

        imageFileOptional = downloadEmbedImages(message, directory);
        if (imageFileOptional.isPresent()) {
            return imageFileOptional;
        }

        if (checkReplies) {
            Message referencedMessage = message.getReferencedMessage();

            if (referencedMessage != null) {
                imageFileOptional = downloadImage(referencedMessage, directory, false);

                if (imageFileOptional.isPresent()) {
                    return imageFileOptional;
                }
            }

            MessageHistory history = message.getChannel().getHistory();
            try {
                List<Message> previousMessages = history.retrievePast(10).submit().get(10, TimeUnit.SECONDS);
                for (Message previousMessage : previousMessages) {
                    Optional<File> previousImageFileOptional = downloadImage(previousMessage, directory, false);

                    if (previousImageFileOptional.isPresent()) {
                        return previousImageFileOptional;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                Main.LOGGER.error("Error while retrieving previous messages", e);
            } catch (TimeoutException e) {
                Main.LOGGER.error("Timeout while retrieving previous messages!", e);
            }
        }

        return Optional.empty();
    }

    private static Optional<File> downloadAttachmentImage(Message message, File directory) {
        List<Message.Attachment> attachments = message.getAttachments();

        for (Message.Attachment attachment : attachments) {
            if (attachment.isImage()) {
                File imageFile = FileUtil.getUniqueFile(directory, attachment.getFileName());

                try {
                    return Optional.of(attachment.downloadToFile(imageFile).get(10, TimeUnit.SECONDS));
                } catch (ExecutionException | InterruptedException e) {
                    Main.LOGGER.error("Error downloading image!", e);
                } catch (TimeoutException e) {
                    Main.LOGGER.error("Image took too long to download!", e);
                }
            }
        }

        return Optional.empty();
    }

    private static Optional<File> downloadUrlImage(String message, File directory, boolean needToExtractUrls) {
        List<String> urls;

        if (needToExtractUrls) {
            urls = extractUrls(message);
        } else {
            urls = ImmutableList.of(message);
        }

        for (String url : urls) {
            try {
                String nameWithoutExtension = Files.getNameWithoutExtension(url);
                String extension = Files.getFileExtension(url);
                String fileName = nameWithoutExtension + "." + extension;

                if (extension.isEmpty()) {
                    extension = "png";
                } else {
                    int index = extension.indexOf("?");
                    if (index != -1) {
                        extension = extension.substring(0, index);
                    }
                }

                BufferedImage image;
                File imageFile = FileUtil.getUniqueFile(directory, fileName);

                if (extension.equals("gif")) {
                    FileUtil.downloadFile(url, imageFile);
                    return Optional.of(imageFile);
                } else {
                    image = ImageIO.read(new URL(url));
                }

                if (image != null) {
                    ImageIO.write(image, extension, imageFile);
                    return Optional.of(imageFile);
                }
            } catch (IOException ignored) {
            }
        }

        return Optional.empty();
    }

    private static Optional<File> downloadEmbedImages(Message message, File directory) {
        List<MessageEmbed> embeds = message.getEmbeds();

        for (MessageEmbed embed : embeds) {
            MessageEmbed.ImageInfo imageInfo = embed.getImage();
            if (imageInfo != null) {

                Optional<File> imageFileOptional = downloadUrlImage(imageInfo.getUrl(), directory, false);
                if (imageFileOptional.isPresent()) {
                    return imageFileOptional;
                }
            }
        }

        return Optional.empty();
    }

    private static List<String> extractUrls(String text) {
        List<String> urls = new ArrayList<>();

        String regex = "\\b((?:https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:, .;]*[-a-zA-Z0-9+&@#/%=~_|])";

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            urls.add(text.substring(matcher.start(0), matcher.end(0)));
        }

        return urls;
    }
}
