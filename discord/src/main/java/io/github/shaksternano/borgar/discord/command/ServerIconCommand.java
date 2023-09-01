package io.github.shaksternano.borgar.discord.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.discord.util.MessageUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ServerIconCommand extends SimpleCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ServerIconCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected String response(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        var message = event.getMessage();
        var serverIconUrl = message.getGuild().getIconUrl();
        return serverIconUrl == null ? "No server icon set!" : MessageUtil.enlargeImageUrl(serverIconUrl);
    }
}
