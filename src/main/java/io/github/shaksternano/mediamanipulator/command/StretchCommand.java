package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.io.*;
import io.github.shaksternano.mediamanipulator.io.mediareader.FFmpegAudioReader;
import io.github.shaksternano.mediamanipulator.io.mediareader.FFmpegImageReader;
import io.github.shaksternano.mediamanipulator.io.mediareader.MediaReader;
import io.github.shaksternano.mediamanipulator.io.mediawriter.MediaWriter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bytedeco.javacv.Frame;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A command that stretches media.
 */
public class StretchCommand extends FileCommand {

    /**
     * The default stretch width multiplier.
     */
    public static final float DEFAULT_WIDTH_MULTIPLIER = 2;

    /**
     * The default stretch height multiplier.
     */
    public static final float DEFAULT_HEIGHT_MULTIPLIER = 1;

    private final boolean RAW;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public StretchCommand(String name, String description, boolean raw) {
        super(name, description);
        RAW = raw;
    }

    @Override
    public File editFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
        float widthMultiplier = CommandParser.parseFloatArgument(
                arguments,
                0,
                DEFAULT_WIDTH_MULTIPLIER,
                null,
                event.getChannel(),
                (argument, defaultValue) -> "Width multiplier \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );
        float heightMultiplier = CommandParser.parseFloatArgument(
                arguments,
                1,
                DEFAULT_HEIGHT_MULTIPLIER,
                null,
                event.getChannel(),
                (argument, defaultValue) -> "Height multiplier \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );

        String outputName = "stretched";
        if (!fileFormat.isBlank()) {
            outputName += "." + fileFormat;
        }
        File output = FileUtil.getUniqueTempFile(outputName);
        try (
                MediaReader<BufferedImage> imageReader = new FFmpegImageReader(file);
                MediaReader<Frame> audioReader = new FFmpegAudioReader(file);
                MediaWriter videoWriter = MediaWriters.createWriter(
                        output,
                        fileFormat,
                        imageReader.getFrameRate(),
                        audioReader.getAudioChannels()
                )
        ) {
            for (BufferedImage imageFrame : imageReader) {
                BufferedImage stretched = ImageUtil.stretch(
                        imageFrame,
                        (int) (imageFrame.getWidth() * widthMultiplier),
                        (int) (imageFrame.getHeight() * heightMultiplier),
                        RAW
                );
                videoWriter.recordImageFrame(stretched);
            }
            for (Frame audioFrame : audioReader) {
                videoWriter.recordAudioFrame(audioFrame);
            }
        }
        return output;
    }
}
