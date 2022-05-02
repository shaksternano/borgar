package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Where {@link Command}s are registered.
 */
public class CommandRegistry {

    /**
     * Stores the registered {@link Command}s.
     */
    private static final Map<String, Command> registry = new HashMap<>();

    /**
     * Registers {@link Command}s.
     *
     * @param commands The commands to register.
     */
    public static void register(Iterable<Command> commands) {
        for (Command command : commands) {
            registry.put(command.getName(), command);
        }
    }

    /**
     * Gets a {@link Command} by its name.
     *
     * @param name The name of the command.
     * @return An {@link Optional} describing the command.
     * The {@link Optional} will be empty if and only if
     * no command was registered with the given name.
     */
    public static Optional<Command> getCommand(String name) {
        return Optional.ofNullable(registry.get(name));
    }

    /**
     * Gets a list of all registered {@link Command}s.
     *
     * @return A list of all registered {@link Command}s.
     */
    public static List<Command> getCommands() {
        return ImmutableList.copyOf(registry.values());
    }
}
