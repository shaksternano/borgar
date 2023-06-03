package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ServerCountCommand extends SimpleCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ServerCountCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected String response(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        var serverCount = event.getJDA().getGuildCache().size();
        var message = "This bot is in " + serverCount + " server";
        if (serverCount != 1) {
            message += "s";
        }
        return message + ".";
    }
}
