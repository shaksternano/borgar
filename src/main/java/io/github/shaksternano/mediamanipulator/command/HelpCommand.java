package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCommand extends Command {

    public static final HelpCommand INSTANCE = new HelpCommand("help", "Lists all commands");

    protected HelpCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        userMessage.reply(getHelpMessage()).queue();
    }

    public static Message getHelpMessage() {
        return new MessageBuilder("Hello").build();
    }
}
