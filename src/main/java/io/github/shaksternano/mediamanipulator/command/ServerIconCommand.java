package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ServerIconCommand extends BaseCommand {

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
    public void execute(String[] arguments, MessageReceivedEvent event) {
        Message message = event.getMessage();
        String serverIconUrl = message.getGuild().getIconUrl();
        message.reply(serverIconUrl == null ? "No server icon set!" : serverIconUrl + "?size=1024").queue();
    }
}
