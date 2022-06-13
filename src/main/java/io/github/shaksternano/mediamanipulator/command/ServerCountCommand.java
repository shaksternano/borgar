package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ServerCountCommand extends BaseCommand {

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
    public void execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        long serverCount = event.getJDA().getGuildCache().size();
        StringBuilder builder = new StringBuilder();
        builder.append("This bot is in ").append(serverCount).append(" server.");
        if (serverCount != 1) {
            builder.insert(builder.length() - 1, "s");
        }
        event.getMessage().reply(builder).queue();
    }
}
