package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ServerSplashCommand extends Command {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ServerSplashCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) {
        Message message = event.getMessage();
        String serverSplashUrl = message.getGuild().getSplashUrl();
        message.reply(serverSplashUrl == null ? "No server invite background image set!" : serverSplashUrl).queue();
    }
}
