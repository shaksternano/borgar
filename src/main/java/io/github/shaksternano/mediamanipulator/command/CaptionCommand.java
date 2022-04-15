package io.github.shaksternano.mediamanipulator.command;

import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.mediamanipulation.ImageManipulator;
import io.github.shaksternano.mediamanipulator.mediamanipulation.MediaManipulatorRegistry;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CaptionCommand extends Command {

    public static final CaptionCommand INSTANCE = new CaptionCommand("caption");

    protected CaptionCommand(String name) {
        super(name);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public Message execute(String[] arguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        List<Message.Attachment> attachments = userMessage.getAttachments();

        if (attachments.size() > 0) {
            Message.Attachment attachment = attachments.get(0);
            String unsupportedFileType = "Unsupported file type!";

            if (attachment.isImage() || attachment.isVideo()) {
                File file = FileUtil.getUniqueFile(new File(FileUtil.getTempDirectory(), attachment.getFileName()), false);
                file.deleteOnExit();
                attachment.downloadToFile(file).thenAccept(mediaFile -> {
                    String fileExtension = Files.getFileExtension(mediaFile.getName());

                    MediaManipulatorRegistry.getManipulator(fileExtension).ifPresentOrElse(mediaManipulator -> {
                        try {
                            File captionedMedia = mediaManipulator.caption(mediaFile, String.join(" ", arguments));
                            captionedMedia.deleteOnExit();
                            userMessage.reply(captionedMedia).queue(message -> {
                                file.delete();
                                captionedMedia.delete();
                            }, throwable -> {
                                file.delete();
                                captionedMedia.delete();
                            });
                        } catch (IOException e) {
                            String errorMessage = "Error captioning media!";
                            userMessage.reply(errorMessage).queue();
                            Main.LOGGER.error(errorMessage, e);
                        }
                    }, () -> userMessage.reply(unsupportedFileType).queue());
                });
            } else {
                userMessage.reply(unsupportedFileType).queue();
            }
        } else {
            userMessage.reply("No media found!").queue();
        }

        return null;
    }
}
