package io.github.shaksternano.mediamanipulator.command.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.command.Command;
import io.github.shaksternano.mediamanipulator.exception.InvalidArgumentException;
import io.github.shaksternano.mediamanipulator.exception.MissingArgumentException;
import io.github.shaksternano.mediamanipulator.util.DiscordUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

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
        String stringMessage = DiscordUtil.getContentStrippedKeepEmotes(userMessage).trim();
        List<String> commandParts = parseCommandParts(stringMessage);

        MessageChannel channel = event.getChannel();

        if (commandParts.size() > 0) {
            Optional<Command> commandOptional = CommandRegistry.getCommand(commandParts.get(0));

            commandOptional.ifPresent(command -> {
                try {
                    channel.sendTyping().queue();
                    List<String> arguments = parseBaseArguments(commandParts, command);
                    ListMultimap<String, String> extraArguments = parseExtraArguments(commandParts, command);

                    try {
                        command.execute(arguments, extraArguments, event);
                    } catch (PermissionException e) {
                        userMessage.reply("This bot doesn't have the required permissions to execute this command!").queue();
                        Main.getLogger().error("This bot doesn't have the required permissions needed to execute command " + command.getNameWithPrefix() + "!", e);
                    } catch (InvalidArgumentException e) {
                        userMessage.reply(e.getMessage() == null ? "Invalid arguments!" : "Invalid arguments: " + e.getMessage()).queue();
                    } catch (MissingArgumentException e) {
                        userMessage.reply(e.getMessage() == null ? "Missing arguments!" : "Missing arguments: " + e.getMessage()).queue();
                    } catch (OutOfMemoryError e) {
                        userMessage.reply("The server ran out of memory trying to execute this command! Try again later.").queue();
                        Main.getLogger().error("Ran out of memory trying to execute command " + command.getNameWithPrefix() + "!", e);
                    } catch (Throwable t) {
                        userMessage.reply("Error executing command!").queue();
                        Main.getLogger().error("Error executing command " + command.getNameWithPrefix() + "!", t);
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
    private static List<String> parseCommandParts(String message) {
        if (message.length() > 1) {
            if (message.startsWith(Command.PREFIX)) {
                String[] commandParts = message.split("\\s+");
                commandParts[0] = commandParts[0].substring(1).toLowerCase();
                return ImmutableList.copyOf(commandParts);
            }
        }

        return ImmutableList.of();
    }

    /**
     * Removes the command word from a message array, leaving only the arguments of the command.
     *
     * @param commandParts The command parts to remove the command word from.
     * @return The arguments of the command.
     */
    private static List<String> parseBaseArguments(List<String> commandParts, Command command) {
        ImmutableList.Builder<String> argumentsBuilder = new ImmutableList.Builder<>();

        boolean passedFirst = false;
        for (String commandPart : commandParts) {
            if (passedFirst) {
                if (commandPart.startsWith(Command.PREFIX)) {
                    String commandWord = commandPart.substring(1).toLowerCase();

                    if (command.getAdditionalParameterNames().contains(commandWord)) {
                        break;
                    }
                }

                argumentsBuilder.add(commandPart);
            } else {
                passedFirst = true;
            }
        }

        return argumentsBuilder.build();
    }

    private static ListMultimap<String, String> parseExtraArguments(List<String> commandParts, Command command) {
        ImmutableListMultimap.Builder<String, String> argumentsBuilder = new ImmutableListMultimap.Builder<>();
        Set<String> passedExtraCommandWords = new HashSet<>();

        String currentExtraParameterName = null;
        for (String commandPart : commandParts) {
            if (commandPart.startsWith(Command.PREFIX)) {
                String commandWord = commandPart.substring(1).toLowerCase();

                if (command.getAdditionalParameterNames().contains(commandWord)
                        && !passedExtraCommandWords.contains(commandWord)
                ) {
                    currentExtraParameterName = commandWord;
                    passedExtraCommandWords.add(commandWord);
                    continue;
                }
            }

            if (currentExtraParameterName != null) {
                argumentsBuilder.put(currentExtraParameterName, commandPart);
            }
        }

        return argumentsBuilder.build();
    }

    public static int parseIntegerArgument(List<String> arguments, int toParseIndex, int defaultValue, @Nullable Predicate<Integer> constraint, MessageChannel triggerChannel, BiFunction<String, String, String> errorMessage) {
        if (arguments.size() > toParseIndex) {
            String argument = arguments.get(toParseIndex);

            try {
                int result = Integer.decode(argument);
                if (constraint == null || constraint.test(result)) {
                    return result;
                }
            } catch (NumberFormatException ignored) {
            }

            triggerChannel.sendMessage(errorMessage.apply(argument, String.valueOf(defaultValue))).queue();
            triggerChannel.sendTyping().queue();
        }

        return defaultValue;
    }

    public static float parseFloatArgument(List<String> arguments, int toParseIndex, float defaultValue, @Nullable Predicate<Float> constraint, MessageChannel triggerChannel, BiFunction<String, String, String> errorMessage) {
        if (arguments.size() > toParseIndex) {
            String argument = arguments.get(toParseIndex);

            try {
                float result = Float.parseFloat(argument);
                if (constraint == null || constraint.test(result)) {
                    return result;
                }
            } catch (NumberFormatException ignored) {
            }

            triggerChannel.sendMessage(errorMessage.apply(argument, FORMAT.format(defaultValue))).queue();
            triggerChannel.sendTyping().queue();
        }

        return defaultValue;
    }

    public static float parseFloatExtraArgument(ListMultimap<String, String> extraArguments, String parameterName, float defaultValue, @Nullable Predicate<Float> constraint, MessageChannel triggerChannel, BiFunction<String, String, String> errorMessage) {
        if (extraArguments.containsKey(parameterName)) {
            String argument = extraArguments.get(parameterName).get(0);

            try {
                float result = Float.parseFloat(argument);
                if (constraint == null || constraint.test(result)) {
                    return Float.parseFloat(argument);
                }
            } catch (NumberFormatException ignored) {
            }

            triggerChannel.sendMessage(errorMessage.apply(argument, FORMAT.format(defaultValue))).queue();
            triggerChannel.sendTyping().queue();
        }

        return defaultValue;
    }
}
