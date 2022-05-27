package io.github.shaksternano.mediamanipulator.command.util;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.command.Command;
import io.github.shaksternano.mediamanipulator.exception.InvalidArgumentException;
import io.github.shaksternano.mediamanipulator.exception.MissingArgumentException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Parses commands from messages.
 */
public class CommandParser {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.####");

    /**
     * Gets a {@link Command} from the command word in a message and executes it.
     *
     * @param event The {@link MessageReceivedEvent} that triggered the command.
     */
    public static void parseAndExecute(MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        String stringMessage = userMessage.getContentRaw().trim();
        String[] commandParts = parseCommandParts(stringMessage);

        MessageChannel channel = event.getChannel();

        if (commandParts.length > 0) {
            Optional<Command> commandOptional = CommandRegistry.getCommand(commandParts[0]);

            commandOptional.ifPresent(command -> {
                try {
                    channel.sendTyping().complete();
                    String[] arguments = parseArguments(commandParts);

                    try {
                        command.execute(arguments, event);
                    } catch (PermissionException e) {
                        userMessage.reply("This bot doesn't have the required permissions to execute this command!").queue();
                        Main.getLogger().error("This bot doesn't have the required permissions needed to execute command " + command.getName() + "!", e);
                    } catch (InvalidArgumentException e) {
                        userMessage.reply(e.getMessage() == null ? "Invalid arguments!" : "Invalid arguments: " + e.getMessage()).queue();
                        Main.getLogger().warn("Invalid arguments for command " + command.getName() + "!", e);
                    } catch (MissingArgumentException e) {
                        userMessage.reply(e.getMessage() == null ? "Missing arguments!" : "Missing arguments: " + e.getMessage()).queue();
                        Main.getLogger().warn("Missing arguments for command " + command.getName() + "!", e);
                    } catch (OutOfMemoryError e) {
                        userMessage.reply("The server ran out of memory trying to execute this command! Try again later.").queue();
                        Main.getLogger().error("Ran out of memory trying to execute command " + command.getName() + "!", e);
                    } catch (Throwable t) {
                        userMessage.reply("Error executing command!").queue();
                        Main.getLogger().error("Error executing command " + command.getName() + "!", t);
                    }
                } catch (PermissionException e) {
                    Main.getLogger().error("Missing send message permission!", e);
                }
            });
        }
    }

    /**
     * Splits a message into a string array, splitting on spaces.
     *
     * @param message The message to split.
     * @return The split message.
     */
    private static String[] parseCommandParts(String message) {
        if (message.length() > 1) {
            if (message.startsWith(Command.PREFIX)) {
                String[] commandParts = message.split("\\s+");
                commandParts[0] = commandParts[0].substring(1).toLowerCase();

                return commandParts;
            }
        }

        return new String[0];
    }

    /**
     * Removes the command word from a message array, leaving only the arguments of the command.
     *
     * @param commandParts The command parts to remove the command word from.
     * @return The arguments of the command.
     */
    private static String[] parseArguments(String[] commandParts) {
        String[] arguments;

        if (commandParts.length > 1) {
            arguments = Arrays.copyOfRange(commandParts, 1, commandParts.length);
        } else {
            arguments = new String[0];
        }

        return arguments;
    }

    public static int parseIntegerArgument(String[] arguments, int toParseIndex, int defaultValue, MessageChannel triggerChannel, BiFunction<String, String, String> errorMessage) {
        if (arguments.length > toParseIndex) {
            String argument = arguments[toParseIndex];

            try {
                return Integer.decode(argument);
            } catch (NumberFormatException e) {
                triggerChannel.sendMessage(errorMessage.apply(argument, String.valueOf(defaultValue))).complete();
                triggerChannel.sendTyping().complete();
            }
        }

        return defaultValue;
    }

    public static float parseFloatArgument(String[] arguments, int toParseIndex, float defaultValue, MessageChannel triggerChannel, BiFunction<String, String, String> errorMessage) {
        if (arguments.length > toParseIndex) {
            String argument = arguments[toParseIndex];

            try {
                return Float.parseFloat(argument);
            } catch (NumberFormatException e) {
                triggerChannel.sendMessage(errorMessage.apply(argument, FORMAT.format(defaultValue))).complete();
                triggerChannel.sendTyping().complete();
            }
        }

        return defaultValue;
    }
}
