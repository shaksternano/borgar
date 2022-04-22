package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A command that is executed when a user sends a certain message. Commands are registered in {@link Commands}.
 */
public abstract class Command implements Comparable<Command> {

    /**
     * The command prefix.
     */
    public static final String COMMAND_PREFIX = "%";

    /**
     * The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
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
     * @param name        The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    protected Command(String name, String description) {
        NAME = name;
        DESCRIPTION = description;
    }

    /**
     * Executes the command.
     *
     * @param arguments The arguments of the command.
     * @param event     The {@link MessageReceivedEvent} that triggered the command.
     * @throws IllegalArgumentException If an argument is invalid.
     * @throws MissingArgumentException If the operation requires an argument but none was provided.
     */
    public abstract void execute(String[] arguments, MessageReceivedEvent event);

    /**
     * Gets the name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
     * followed by this name, the command will be executed.
     *
     * @return The name of the command.
     */
    public String getName() {
        return NAME;
    }

    /**
     * Gets the description of the command. This is displayed in the help command.
     *
     * @return The description of the command.
     */
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Compares this command to another command using {@link #getName()}.
     *
     * @param command The command to compare to.
     * @return The result of the comparison.
     */
    @Override
    public int compareTo(@NotNull Command command) {
        return getName().compareTo(command.getName());
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
