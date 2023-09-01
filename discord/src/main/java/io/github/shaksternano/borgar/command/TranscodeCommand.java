package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.media.MediaUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TranscodeCommand extends FileCommand {

    private final String outputFormat;

    public TranscodeCommand(String outputFormat) {
        super(outputFormat, "Converts media to a `" + outputFormat.toLowerCase() + "` file.");
        this.outputFormat = outputFormat;
    }

    @Override
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        return new NamedFile(
            MediaUtil.transcode(
                file,
                fileName,
                fileFormat,
                outputFormat,
                maxFileSize
            ),
            Files.getNameWithoutExtension(fileName),
            outputFormat
        );
    }
}
