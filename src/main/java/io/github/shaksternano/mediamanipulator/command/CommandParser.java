package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Optional;

/**
 * Parses commands from messages.
 */
public class CommandParser {

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
            channel.sendTyping().queue(unused -> {
                Optional<Command> commandOptional = CommandRegistry.getCommand(commandParts[0]);

                commandOptional.ifPresentOrElse(command -> {
                    String[] arguments = parseArguments(commandParts);
                    try {
                        command.execute(arguments, event);
                    } catch (IllegalArgumentException | MissingArgumentException e) {
                        userMessage.reply(e.getMessage() == null ? "Missing arguments!" : e.getMessage()).queue();
                    } catch (OutOfMemoryError e) {
                        userMessage.reply("The server ran out of memory trying to execute this command! Try again later.").queue();
                        Main.LOGGER.error("Ran out of memory trying to execute " + command + "!", e);
                    } catch (Throwable t) {
                        userMessage.reply("Error executing command!").queue();
                        Main.LOGGER.error("Error executing command " + command + "!", t);
                    }
                }, () -> userMessage.reply("Invalid command!").queue());
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
            if (message.startsWith(Command.COMMAND_PREFIX)) {
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
}
