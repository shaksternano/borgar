package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.Main;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public abstract class BotOwnerCommand extends SimpleCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public BotOwnerCommand(String name, String description) {
        super(name, description + " Only the owner of this bot can use this command.");
    }

    @Override
    protected final String response(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        var userMessage = event.getMessage();
        var userId = userMessage.getAuthor().getIdLong();
        if (userId == Main.getOwnerId()) {
            return botOwnerOperation(arguments, extraArguments, event);
        } else {
            return "Only the owner of this bot can use this command!";
        }
    }

    protected abstract String botOwnerOperation(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event);
}
