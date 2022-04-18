package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ImmutableList;

import java.util.*;

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

    public static List<Command> getCommands() {
        return ImmutableList.copyOf(registry.values());
    }
}
