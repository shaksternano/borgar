package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.exception.InvalidMediaException;
import io.github.shaksternano.mediamanipulator.exception.UnsupportedFileFormatException;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import io.github.shaksternano.mediamanipulator.mediamanipulator.util.MediaManipulatorRegistry;
import io.github.shaksternano.mediamanipulator.util.DiscordUtil;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

public abstract class OptionalFileInputMediaCommand extends BaseCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public OptionalFileInputMediaCommand(String name, String description) {
        super(name, description);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();

        File file = arguments.length == 0 ? MessageUtil.downloadImage(userMessage, FileUtil.getTempDir().toString()).orElse(null) : null;
        String fileFormat = file == null ? null : FileUtil.getFileFormat(file);
        MediaManipulator manipulator = fileFormat == null ? null : MediaManipulatorRegistry.getManipulator(fileFormat).orElse(null);

        try {
            File editedMedia;

            if (file == null) {
                editedMedia = applyOperation(arguments, event);
            } else {
                editedMedia = applyOperation(file, fileFormat, arguments, manipulator, event);
            }

            String newFileFormat = FileUtil.getFileFormat(editedMedia);
            File compressedMedia;
            Optional<MediaManipulator> manipulatorOptional = MediaManipulatorRegistry.getManipulator(newFileFormat);
            if (manipulatorOptional.isPresent()) {
                compressedMedia = manipulatorOptional.orElseThrow().compress(editedMedia, newFileFormat, event.getGuild());
            } else {
                compressedMedia = editedMedia;
            }

            if (file != null) {
                file.delete();
            }

            long mediaFileSize = compressedMedia.length();
            if (mediaFileSize > DiscordUtil.getMaxUploadSize(event.getGuild())) {
                long mediaFileSizeInMb = mediaFileSize / (1024 * 1024);
                userMessage.reply("The size of the edited media file, " + mediaFileSizeInMb + "MB, is too large to send!").queue();
                Main.getLogger().error("File size of edited media was too large to send! (" + mediaFileSize + "MB)");
                editedMedia.delete();
                compressedMedia.delete();
            } else {
                userMessage.reply(compressedMedia).queue(message -> {
                    editedMedia.delete();
                    compressedMedia.delete();
                }, throwable -> {
                    editedMedia.delete();
                    compressedMedia.delete();
                    String failSend = "Failed to send edited media!";

                    userMessage.reply(failSend).queue();
                    Main.getLogger().error(failSend, throwable);
                });
            }
        } catch (InvalidMediaException e) {
            userMessage.reply(e.getMessage() == null ? "Invalid media!" : "Invalid media: " + e.getMessage()).queue();
            Main.getLogger().error("Invalid media!", e);
        } catch (UnsupportedFileFormatException e) {
            String unsupportedMessage = "This operation is not supported on files with type \"" + fileFormat + "\"!";

            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                unsupportedMessage = unsupportedMessage + " Reason: " + e.getMessage();
            }

            userMessage.reply(unsupportedMessage).queue();
            Main.getLogger().warn("Unsupported operation!", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (OutOfMemoryError e) {
            userMessage.reply("The server ran out of memory! Try again later or use a smaller file.").queue();
            Main.getLogger().error("Ran out of memory executing command " + getName() + "!", e);
        }
    }

    public abstract File applyOperation(File media, String fileFormat, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException;

    public abstract File applyOperation(String[] arguments, MessageReceivedEvent event) throws IOException;
}
