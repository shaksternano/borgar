package io.github.shaksternano.mediamanipulator.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum CommandRegistry {

    INSTANCE;

    private final Map<String, Command> commands = new HashMap<>();

    public void register(Command... commandArray) {
        for (Command command : commandArray) {
            commands.put(command.getName(), command);
        }
    }

    public Optional<Command> getCommand(String name) {
        return Optional.ofNullable(commands.get(name));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Command command : commands.values()) {
            builder.append(command).append("\n");
        }

        return builder.toString();
    }
}
