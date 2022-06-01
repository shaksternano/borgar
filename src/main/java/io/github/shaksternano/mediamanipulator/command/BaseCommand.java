package io.github.shaksternano.mediamanipulator.command;

import java.util.Objects;

/**
 * This class provides some default implementations for the {@code Command} interface.
 */
public abstract class BaseCommand implements Command {

    /**
     * The unique name of the command. When a user sends a message starting with {@link Command#PREFIX}
     * followed by this name, the command will be executed.
     */
    private final String NAME;

    /**
     * The description of the command. This is displayed in the help command.
     */
    private final String DESCRIPTION;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public BaseCommand(String name, String description) {
        NAME = name;
        DESCRIPTION = description;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getNameWithPrefix() {
        return Command.PREFIX + NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME, DESCRIPTION);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BaseCommand other) {
            return Objects.equals(NAME, other.NAME);
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
