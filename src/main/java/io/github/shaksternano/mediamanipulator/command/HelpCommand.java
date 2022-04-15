package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCommand extends Command {

    public static final HelpCommand INSTANCE = new HelpCommand("help");

    protected HelpCommand(String name) {
        super(name);
    }

    @Override
    public Message execute(String[] arguments, MessageReceivedEvent event) {
        return getHelpMessage();
    }

    public static Message getHelpMessage() {
        return new MessageBuilder("Hello").build();
    }
}
