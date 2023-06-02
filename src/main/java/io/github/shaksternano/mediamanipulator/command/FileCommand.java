package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.io.NamedFile;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract non-sealed class FileCommand extends BaseFileCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public FileCommand(String name, String description) {
        super(name, description, true);
    }

    @Override
    protected abstract NamedFile modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException;

    @Override
    protected final NamedFile createFile(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) {
        throw new IllegalStateException("This method should never be called");
    }
}
