package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.exception.UnsupportedFileFormatException;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.io.NamedFile;
import io.github.shaksternano.mediamanipulator.media.MediaUtil;
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
    protected NamedFile modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        if (fileFormat.equals(outputFormat)) {
            throw new UnsupportedFileFormatException("The file is already a `" + outputFormat.toLowerCase() + "` file!");
        }
        return new NamedFile(
            MediaUtil.transcode(
                file,
                fileFormat,
                outputFormat,
                maxFileSize
            ),
            FileUtil.changeExtension(file.getName(), outputFormat)
        );
    }
}
