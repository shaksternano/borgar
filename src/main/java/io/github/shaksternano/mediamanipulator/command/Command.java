package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public abstract class Command implements Comparable<Command> {

    private final String NAME;
    private final String DESCRIPTION;

    protected Command(String name, String description) {
        NAME = name;
        DESCRIPTION = description;
    }

    public abstract void execute(String[] arguments, MessageReceivedEvent event);

    public String getName() {
        return NAME;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public int compareTo(@NotNull Command command) {
        return getName().compareTo(command.getName());
    }

    @Override
    public String toString() {
        return "Command: " + getName() + ", description: " + getDescription();
    }
}
