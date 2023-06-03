package io.github.shaksternano.borgar.command;

import java.util.Objects;
import java.util.Set;

/**
 * This class provides some default implementations for the {@code Command} interface.
 */
public abstract class BaseCommand implements Command {

    /**
     * The unique name of the command. When a user sends a message starting with {@link Command#PREFIX}
     * followed by this name, the command will be executed.
     */
    private final String name;

    /**
     * The description of the command. This is displayed in the help command.
     */
    private final String description;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public BaseCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public Set<String> getAdditionalParameterNames() {
        return Set.of();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNameWithPrefix() {
        return Command.PREFIX + name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int compareTo(Command o) {
        return name.compareTo(o.getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BaseCommand other) {
            return Objects.equals(name, other.name);
        } else {
            return false;
        }
    }

    /**
     * Gets the name and description of the command in a formatted string.
     *
     * @return The name and description of the command in a formatted string.
     */
    @Override
    public String toString() {
        return "Command: " + getNameWithPrefix() + ", description: " + getDescription();
    }
}
