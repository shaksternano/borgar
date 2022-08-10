package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.Commands;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Set;

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
     * @param arguments      The arguments of the command.
     * @param extraArguments A multimap mapping the additional parameter names to a list of the arguments.
     * @param event          The event that triggered the command.
     */
    void execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws Exception;

    Set<String> getAdditionalParameterNames();

    /**
     * Gets the name of the command. When a user sends a message starting with {@link #PREFIX}
     * followed by this name, the command will be executed.
     *
     * @return The name of the command.
     */
    String getName();

    /**
     * Gets the name of the command prepended with the {@link #PREFIX}.
     *
     * @return The name of the command prepended with the {@link #PREFIX}.
     */
    String getNameWithPrefix();

    /**
     * Gets the description of the command..
     *
     * @return The description of the command.
     */
    String getDescription();
}
