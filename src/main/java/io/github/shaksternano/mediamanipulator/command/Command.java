package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {

    private final String NAME;

    protected Command(String name) {
        NAME = name;
    }

    public abstract Message execute(String[] arguments, MessageReceivedEvent event);

    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return "Command: " + getName();
    }
}
