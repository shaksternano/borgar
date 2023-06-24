package io.github.shaksternano.borgar.command.util;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import io.github.shaksternano.borgar.Main;
import io.github.shaksternano.borgar.command.Command;
import io.github.shaksternano.borgar.exception.InvalidArgumentException;
import io.github.shaksternano.borgar.exception.MissingArgumentException;
import io.github.shaksternano.borgar.util.CompletableFutureUtil;
import io.github.shaksternano.borgar.util.DiscordUtil;
import io.github.shaksternano.borgar.util.MiscUtil;
import io.github.shaksternano.borgar.util.StringUtil;
import io.github.shaksternano.borgar.util.function.FloatPredicate;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * Parses commands from messages.
 */
public class CommandParser {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.####");

    public static void parseAndExecute(MessageReceivedEvent event) {
        DiscordUtil.getContentStrippedKeepEmotes(event.getMessage()).thenAccept(stringMessage -> {
            var commandParts = parseCommandParts(stringMessage.trim());
            if (commandParts.size() > 0) {
                CommandRegistry.getCommand(commandParts.get(0))
                    .ifPresent(command -> handleCommand(command, commandParts, event));
            }
        });
    }

    private static <T> void handleCommand(Command<T> command, List<String> commandParts, MessageReceivedEvent event) {
        try {
            if (!authorHasPermissions(command.requiredPermissions(), event)) {
                event.getMessage().reply("You don't have permission to use this command!").queue();
                return;
            }
            var channel = event.getChannel();
            var triggerMessage = event.getMessage();
            channel.sendTyping().queue();
            var arguments = parseBaseArguments(commandParts, command);
            var extraArguments = parseExtraArguments(commandParts, command);
            command.execute(arguments, extraArguments, event)
                .exceptionally(throwable -> handleError(throwable, command))
                .thenCompose(response -> CompletableFutureUtil.forEachSequentiallyAsync(
                    response.responses(),
                    (messageResponse, index) -> {
                        var reply = channel.sendMessage(messageResponse);
                        if (index == 0) {
                            reply.setMessageReference(triggerMessage);
                        }
                        reply.timeout(10, TimeUnit.SECONDS);
                        return MiscUtil.repeatTry(
                            reply::submit,
                            3,
                            5,
                            CommandParser::handleReplyAttemptFailure
                        ).thenAccept(message -> {
                            if (index == 0) {
                                command.handleFirstResponse(message, event, response.responseData());
                            }
                        }).whenComplete((unused, throwable) -> {
                            if (throwable != null) {
                                Main.getLogger().error("Error sending response", throwable);
                                event.getMessage().reply("Error sending response!").queue();
                            }
                        });
                    }
                ));
        } catch (PermissionException e) {
            Main.getLogger().error("Missing send message permission", e);
        }
    }

    private static boolean authorHasPermissions(Collection<Permission> permissions, MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return true;
        }
        var member = event.getMember();
        return member != null && member.hasPermission(permissions);
    }

    private static <T> CommandResponse<T> handleError(Throwable error, Command<?> command) {
        if (error instanceof CompletionException) {
            var cause = error.getCause();
            if (cause != null) {
                error = cause;
            }
        }
        var throwableMessage = error.getMessage();
        String errorMessage;
        if (error instanceof PermissionException) {
            Main.getLogger().error("This bot doesn't have the required permissions needed to execute command " + command.nameWithPrefix(), error);
            errorMessage = "This bot doesn't have the required permissions to execute this command!";
        } else if (error instanceof InvalidArgumentException) {
            errorMessage = StringUtil.nullOrBlank(throwableMessage) ? "Invalid arguments!" : "Invalid arguments: " + throwableMessage;
        } else if (error instanceof MissingArgumentException) {
            errorMessage = StringUtil.nullOrBlank(throwableMessage) ? "Missing arguments!" : "Missing arguments: " + throwableMessage;
        } else if (error instanceof OutOfMemoryError) {
            Main.getLogger().error("Ran out of memory trying to execute command " + command.nameWithPrefix(), error);
            errorMessage = "The server ran out of memory trying to execute this command!";
        } else {
            Main.getLogger().error("Error executing command " + command.nameWithPrefix(), error);
            errorMessage = "Error executing command!";
        }
        return new CommandResponse<>(errorMessage);
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
                return List.of(commandParts);
            }
        }
        return List.of();
    }

    /**
     * Removes the command word from a message array, leaving only the arguments of the command.
     *
     * @param commandParts The command parts to remove the command word from.
     * @return The arguments of the command.
     */
    private static List<String> parseBaseArguments(List<String> commandParts, Command<?> command) {
        List<String> arguments = new ArrayList<>();
        var passedFirst = false;
        for (var commandPart : commandParts) {
            if (passedFirst) {
                if (commandPart.startsWith(Command.PREFIX)) {
                    var commandWord = commandPart.substring(1).toLowerCase();
                    if (command.parameterNames().contains(commandWord)) {
                        break;
                    }
                }
                arguments.add(commandPart);
            } else {
                passedFirst = true;
            }
        }
        return Collections.unmodifiableList(arguments);
    }

    private static ListMultimap<String, String> parseExtraArguments(List<String> commandParts, Command<?> command) {
        ListMultimap<String, String> arguments = MultimapBuilder.hashKeys().arrayListValues().build();
        Set<String> passedExtraCommandWords = new HashSet<>();
        String currentExtraParameterName = null;
        for (var commandPart : commandParts) {
            if (commandPart.startsWith(Command.ARGUMENT_PREFIX)) {
                var commandWord = commandPart.substring(1).toLowerCase();
                if (command.parameterNames().contains(commandWord)
                    && !passedExtraCommandWords.contains(commandWord)
                ) {
                    if (currentExtraParameterName != null && !arguments.containsKey(currentExtraParameterName)) {
                        arguments.put(currentExtraParameterName, "");
                    }
                    currentExtraParameterName = commandWord;
                    passedExtraCommandWords.add(commandWord);
                    continue;
                }
            }
            if (currentExtraParameterName != null) {
                arguments.put(currentExtraParameterName, commandPart);
            }
        }
        if (currentExtraParameterName != null && !arguments.containsKey(currentExtraParameterName)) {
            arguments.put(currentExtraParameterName, "");
        }
        return Multimaps.unmodifiableListMultimap(arguments);
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
