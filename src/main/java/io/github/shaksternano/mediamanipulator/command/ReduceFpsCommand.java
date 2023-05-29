package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.io.NamedFile;
import io.github.shaksternano.mediamanipulator.media.ImageFrame;
import io.github.shaksternano.mediamanipulator.media.io.MediaReaders;
import io.github.shaksternano.mediamanipulator.media.io.MediaWriters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReduceFpsCommand extends FileCommand {

    public static final int DEFAULT_FPS_REDUCTION_MULTIPLIER = 2;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ReduceFpsCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected NamedFile modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
        var fpsReductionRatio = CommandParser.parseFloatArgument(
            arguments,
            0,
            DEFAULT_FPS_REDUCTION_MULTIPLIER,
            ratio -> ratio > 1,
            event.getChannel(),
            (argument, defaultValue) -> "FPS reduction multiplier \"" + argument + "\" is not a number larger than 1! Using default value of " + defaultValue + "."
        );
        return reduceFps(file, fileFormat, fpsReductionRatio);
    }

    private static NamedFile reduceFps(File media, String format, float fpsReductionRatio) throws IOException {
        var nameWithoutExtension = "reduced_fps";
        var output = FileUtil.createTempFile(nameWithoutExtension, format);
        try (
            var imageReader = MediaReaders.createImageReader(media, format);
            var audioReader = MediaReaders.createAudioReader(media, format);
            var writer = MediaWriters.createWriter(
                output,
                format,
                audioReader.audioChannels()
            );
            var audioIterator = audioReader.iterator()
        ) {
            var frameInterval = imageReader.frameDuration() * fpsReductionRatio;
            var timestamp = 0L;
            while (timestamp < imageReader.duration()) {
                var image = imageReader.readFrame(timestamp).content();
                var timeRemaining = imageReader.duration() - timestamp;
                var frameDuration = Math.min(frameInterval, timeRemaining);
                writer.writeImageFrame(new ImageFrame(image, frameDuration, timestamp));
                timestamp += frameDuration;
            }
            while (audioIterator.hasNext()) {
                var audioFrame = audioIterator.next();
                writer.writeAudioFrame(audioFrame);
            }
            return new NamedFile(output, nameWithoutExtension, format);
        }
    }
}
