package io.github.shaksternano.borgar.discord.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.discord.command.util.CommandResponse;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class SimpleCommand extends BaseCommand<Void> {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public SimpleCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public final CompletableFuture<CommandResponse<Void>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        return new CommandResponse<Void>(response(arguments, extraArguments, event)).asFuture();
    }

    protected abstract String response(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event);
}
