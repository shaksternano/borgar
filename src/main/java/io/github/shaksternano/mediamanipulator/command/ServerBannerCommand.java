package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ServerBannerCommand extends Command {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ServerBannerCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) {
        Message message = event.getMessage();
        String serverBannerUrl = message.getGuild().getBannerUrl();
        message.reply(serverBannerUrl == null ? "No server banner set!" : serverBannerUrl).queue();
    }
}
