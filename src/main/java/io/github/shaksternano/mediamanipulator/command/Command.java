package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.command.util.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * A command that is executed when a user sends a certain message. Commands are registered in {@link Commands}.
 */
public interface Command {

    /**
     * The command prefix.
     */
    String PREFIX = "%";

    /**
     * Executes the command.
     *
     * @param arguments The arguments of the command.
     * @param event     The event that triggered the command.
     */
    void execute(String[] arguments, MessageReceivedEvent event);

    /**
     * Gets the name of the command. When a user sends a message starting with {@link #PREFIX}
     * followed by this name, the command will be executed.
     *
     * @return The name of the command.
     */
    String getName();

    /**
     * Gets the description of the command..
     *
     * @return The description of the command.
     */
    String getDescription();
}
