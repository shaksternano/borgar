package io.github.shaksternano.borgar.core.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.core.command.util.CommandParser;
import io.github.shaksternano.borgar.core.io.FileUtil;
import io.github.shaksternano.borgar.core.io.NamedFile;
import io.github.shaksternano.borgar.core.media.MediaReaders;
import io.github.shaksternano.borgar.core.media.MediaUtil;
import io.github.shaksternano.borgar.core.media.imageprocessor.IdentityProcessor;
import io.github.shaksternano.borgar.core.media.reader.ConstantFrameDurationMediaReader;
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
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        var fpsReductionRatio = CommandParser.parseFloatArgument(
            arguments,
            0,
            DEFAULT_FPS_REDUCTION_MULTIPLIER,
            ratio -> ratio > 1,
            event.getChannel(),
            (argument, defaultValue) -> "FPS reduction multiplier \"" + argument + "\" is not a number larger than 1! Using default value of " + defaultValue + "."
        );
        return reduceFps(
            file,
            fileFormat,
            fpsReductionRatio,
            maxFileSize
        );
    }

    private static NamedFile reduceFps(File media, String format, float fpsReductionRatio, long maxFileSize) throws IOException {
        var nameWithoutExtension = "reduced_fps";
        var output = FileUtil.createTempFile(nameWithoutExtension, format);
        var imageReader = MediaReaders.createImageReader(media, format);
        var audioReader = MediaReaders.createAudioReader(media, format);
        var frameInterval = imageReader.frameDuration() * fpsReductionRatio;
        return new NamedFile(
            MediaUtil.processMedia(
                new ConstantFrameDurationMediaReader<>(imageReader, frameInterval),
                audioReader,
                output,
                format,
                IdentityProcessor.INSTANCE,
                maxFileSize
            ),
            nameWithoutExtension,
            format
        );
    }
}
