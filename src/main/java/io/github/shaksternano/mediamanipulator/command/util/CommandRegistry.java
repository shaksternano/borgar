package io.github.shaksternano.mediamanipulator.command.util;

import io.github.shaksternano.mediamanipulator.command.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Where {@link Command}s are registered.
 */
public class CommandRegistry {

    /**
     * Stores the registered {@code Command}s.
     */
    private static final Map<String, Command> registry = new HashMap<>();

    /**
     * Registers {@code Command}s.
     *
     * @param commands The commands to register.
     */
    public static void register(Iterable<Command> commands) {
        for (Command command : commands) {
            registry.put(command.getName().toLowerCase(), command);
        }
    }

    /**
     * Gets a {@code Command} by its name.
     *
     * @param name The name of the command.
     * @return An {@code Optional} describing the command.
     * The {@code Optional} will be empty if and only if
     * no command was registered with the given name.
     */
    public static Optional<Command> getCommand(String name) {
        return Optional.ofNullable(registry.get(name.toLowerCase()));
    }

    /**
     * Gets a set of all registered {@code Command}s.
     *
     * @return A set of all registered {@code Command}s.
     */
    public static Set<Command> getCommands() {
        return Set.copyOf(registry.values());
    }
}
