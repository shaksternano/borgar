package io.github.shaksternano.mediamanipulator.command;

/**
 * This class provides some default implementations for the {@code Command} interface.
 */
public abstract class BaseCommand implements Command {

    /**
     * The name of the command. When a user sends a message starting with {@link Command#PREFIX}
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
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Gets the name and description of the command in a formatted string.
     *
     * @return The name and description of the command in a formatted string.
     */
    @Override
    public String toString() {
        return "Command: " + getName() + ", description: " + getDescription();
    }
}
