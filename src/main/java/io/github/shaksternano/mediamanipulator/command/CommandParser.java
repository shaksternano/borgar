package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Optional;

public class CommandParser {

    private static final String COMMAND_PREFIX = "!";

    public static void parseAndTryExecute(MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        String stringMessage = userMessage.getContentRaw().trim();
        String[] commandParts = parseCommandParts(stringMessage);

        if (commandParts.length > 0) {
            event.getChannel().sendTyping().submit().thenAccept(unused -> {
                Optional<Command> commandOptional = CommandRegistry.INSTANCE.getCommand(commandParts[0]);

                if (commandOptional.isPresent()) {
                    Command command = commandOptional.orElseThrow();
                    String[] arguments = parseArguments(commandParts);

                    Message commandReply = command.execute(arguments, event);
                    userMessage.reply(commandReply).queue();
                } else {
                    userMessage.reply("Invalid command!").queue();
                }
            });
        }
    }

    private static String[] parseCommandParts(String message) {
        if (message.length() > 1) {
            if (message.startsWith(COMMAND_PREFIX)) {
                String[] commandParts = message.split("\\s+");
                commandParts[0] = commandParts[0].substring(1).toLowerCase();

                return commandParts;
            }
        }

        return new String[0];
    }

    private static String[] parseArguments(String[] commandParts) {
        String[] arguments;

        if (commandParts.length > 1) {
            arguments = Arrays.copyOfRange(commandParts, 1, commandParts.length);
        } else {
            arguments = new String[0];
        }

        return arguments;
    }
}
