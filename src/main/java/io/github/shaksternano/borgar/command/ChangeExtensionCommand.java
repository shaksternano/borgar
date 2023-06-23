package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.exception.UnsupportedFileFormatException;
import io.github.shaksternano.borgar.io.FileUtil;
import io.github.shaksternano.borgar.io.NamedFile;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChangeExtensionCommand extends FileCommand {

    private final String newExtension;

    public ChangeExtensionCommand(String newExtension) {
        super(newExtension + 2, "Changes the extension of a file to `." + newExtension.toLowerCase() + "`.");
        this.newExtension = newExtension;
    }

    @Override
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        if (FileUtil.getFileExtension(fileName).equals(newExtension)) {
            throw new UnsupportedFileFormatException("The file already has the extension `." + newExtension.toLowerCase() + "`!");
        }
        return FileUtil.changeFileExtension(file, fileName, newExtension);
    }
}
