package io.github.shaksternano.mediamanipulator.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandRegistry {

    private static final Map<String, Command> registry = new HashMap<>();

    public static void register(Command... commands) {
        for (Command command : commands) {
            registry.put(command.getName(), command);
        }
    }

    public static Optional<Command> getCommand(String name) {
        return Optional.ofNullable(registry.get(name));
    }
}
