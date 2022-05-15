package io.github.shaksternano.mediamanipulator.command.util;

import io.github.shaksternano.mediamanipulator.command.Command;

import java.util.*;

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
     * Gets an unmodifiable view of all registered {@link Command}s.
     *
     * @return An unmodifiable view of all registered {@link Command}s.
     */
    public static Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(registry.values());
    }
}
