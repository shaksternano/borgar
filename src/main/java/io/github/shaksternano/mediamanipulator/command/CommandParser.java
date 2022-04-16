package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Optional;

public class CommandParser {

    private static final String COMMAND_PREFIX = "!";

    public static void parseAndTryExecute(MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        String stringMessage = userMessage.getContentRaw().trim();
        String[] commandParts = parseCommandParts(stringMessage);

        MessageChannel channel = event.getChannel();

        if (commandParts.length > 0) {
            channel.sendMessage("Thinking...").queue(waitingMessage -> channel.sendTyping().queue(unused -> {
                Optional<Command> commandOptional = CommandRegistry.getCommand(commandParts[0]);

                commandOptional.ifPresentOrElse(command -> {
                    String[] arguments = parseArguments(commandParts);
                    command.execute(arguments, event);
                    waitingMessage.delete().queue();
                }, () -> userMessage.reply("Invalid command!").queue());
            }));
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
