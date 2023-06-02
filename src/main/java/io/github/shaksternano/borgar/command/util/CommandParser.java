package io.github.shaksternano.borgar.command.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.Main;
import io.github.shaksternano.borgar.command.Command;
import io.github.shaksternano.borgar.exception.InvalidArgumentException;
import io.github.shaksternano.borgar.exception.MissingArgumentException;
import io.github.shaksternano.borgar.util.DiscordUtil;
import io.github.shaksternano.borgar.util.MessageUtil;
import io.github.shaksternano.borgar.util.MiscUtil;
import io.github.shaksternano.borgar.util.function.FloatPredicate;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.IntPredicate;
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
        var triggerMessage = event.getMessage();
        var stringMessage = DiscordUtil.getContentStrippedKeepEmotes(triggerMessage).trim();
        var commandParts = parseCommandParts(stringMessage);
        var channel = event.getChannel();
        if (commandParts.size() > 0) {
            CommandRegistry.getCommand(commandParts.get(0)).ifPresent(command -> {
                try {
                    channel.sendTyping().queue();
                    var arguments = parseBaseArguments(commandParts, command);
                    var extraArguments = parseExtraArguments(commandParts, command);
                    command.execute(arguments, extraArguments, event)
                        .exceptionally(throwable -> handleError(throwable, command))
                        .thenAccept(responses -> {
                            CompletableFuture<?> future = CompletableFuture.completedFuture(null);
                            for (var i = 0; i < responses.size(); i++) {
                                var response = responses.get(i);
                                var isFirst = i == 0;
                                future = future.thenCompose(unused -> {
                                    var reply = channel.sendMessage(response);
                                    if (isFirst) {
                                        reply.setMessageReference(triggerMessage);
                                    }
                                    return MiscUtil.repeatTry(
                                        reply::submit,
                                        3,
                                        5,
                                        CommandParser::handleReplyAttemptFailure
                                    );
                                });
                            }
                        });
                } catch (PermissionException e) {
                    Main.getLogger().error("Missing send message permission", e);
                }
            });
        }
    }

    private static List<MessageCreateData> handleError(Throwable error, Command command) {
        if (error instanceof CompletionException) {
            var cause = error.getCause();
            if (cause != null) {
                error = cause;
            }
        }
        var throwableMessage = error.getMessage();
        String errorMessage;
        if (error instanceof PermissionException) {
            Main.getLogger().error("This bot doesn't have the required permissions needed to execute command " + command.getNameWithPrefix(), error);
            errorMessage = "This bot doesn't have the required permissions to execute this command!";
        } else if (error instanceof InvalidArgumentException) {
            errorMessage = MiscUtil.nullOrBlank(throwableMessage) ? "Invalid arguments!" : "Invalid arguments: " + throwableMessage;
        } else if (error instanceof MissingArgumentException) {
            errorMessage = MiscUtil.nullOrBlank(throwableMessage) ? "Missing arguments!" : "Missing arguments: " + throwableMessage;
        } else if (error instanceof OutOfMemoryError) {
            Main.getLogger().error("Ran out of memory trying to execute command " + command.getNameWithPrefix(), error);
            errorMessage = "The server ran out of memory trying to execute this command!";
        } else {
            Main.getLogger().error("Error executing command " + command.getNameWithPrefix(), error);
            errorMessage = "Error executing command!";
        }
        return MessageUtil.createResponse(errorMessage);
    }

    private static void handleReplyAttemptFailure(int attempts, Throwable error) {
        Main.getLogger().error(attempts + " failed attempt" + (attempts == 1 ? "" : "s") + " to reply", error);
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
                var commandParts = message.split("\\s+");
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
        var argumentsBuilder = new ImmutableList.Builder<String>();
        var passedFirst = false;
        for (var commandPart : commandParts) {
            if (passedFirst) {
                if (commandPart.startsWith(Command.PREFIX)) {
                    var commandWord = commandPart.substring(1).toLowerCase();
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
        var argumentsBuilder = new ImmutableListMultimap.Builder<String, String>();
        Set<String> passedExtraCommandWords = new HashSet<>();
        String currentExtraParameterName = null;
        for (var commandPart : commandParts) {
            if (commandPart.startsWith(Command.PREFIX)) {
                var commandWord = commandPart.substring(1).toLowerCase();
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

    public static int parseIntegerArgument(List<String> arguments, int toParseIndex, int defaultValue, @Nullable IntPredicate constraint, MessageChannel triggerChannel, BiFunction<String, String, String> errorMessage) {
        if (arguments.size() > toParseIndex) {
            var argument = arguments.get(toParseIndex);
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

    public static float parseFloatArgument(List<String> arguments, int toParseIndex, float defaultValue, @Nullable FloatPredicate constraint, MessageChannel triggerChannel, BiFunction<String, String, String> errorMessage) {
        if (arguments.size() > toParseIndex) {
            var argument = arguments.get(toParseIndex);
            try {
                var result = Float.parseFloat(argument);
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
            var argument = extraArguments.get(parameterName).get(0);
            try {
                var result = Float.parseFloat(argument);
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
