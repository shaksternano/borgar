package io.github.shaksternano.borgar.core.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.core.command.util.CommandRegistry;
import io.github.shaksternano.borgar.core.command.util.CommandResponse;
import io.github.shaksternano.borgar.core.data.repository.TemplateRepository;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.SplitUtil;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A command that displays the all registered commands.
 */
public class HelpCommand extends BaseCommand<Void> {

    private static final Map<Long, List<String>> cachedHelpMessages = new ConcurrentHashMap<>();

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public HelpCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Sends the command list in the channel the command was triggered in.
     *
     * @param arguments      The arguments of the command.
     * @param extraArguments A multimap mapping the additional parameter names to a list of the arguments.
     * @param event          The {@link MessageReceivedEvent} that triggered the command.
     * @return A {@code CompletableFuture} that completes with a list of {@code MessageCreateData} that will be sent to the channel where the command was triggered.
     */
    @Override
    public CompletableFuture<CommandResponse<Void>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        var entityId = event.isFromGuild() ? event.getGuild().getIdLong() : event.getAuthor().getIdLong();
        return getHelpMessages(entityId).thenApply(messages -> new CommandResponse<Void>(messages).withSuppressEmbeds(true));
    }

    /**
     * Gets the messages to be displayed when this command is run.
     *
     * @param entityId The ID of the guild the command was run in, or the ID of the user if the command was run in a DM.
     * @return The messages to be displayed when this command is run.
     */
    public static CompletableFuture<List<MessageCreateData>> getHelpMessages(long entityId) {
        var cachedMessage = cachedHelpMessages.get(entityId);
        if (cachedMessage != null) {
            return CompletableFuture.completedFuture(cachedMessage.stream()
                .map(MessageCreateData::fromContent)
                .toList()
            );
        }
        return getCommandInfos(entityId).thenApply(commandInfos -> {
            var helpMessages = createHelpMessages(commandInfos);
            cachedHelpMessages.put(entityId, helpMessages);
            return helpMessages.stream()
                .map(MessageCreateData::fromContent)
                .toList();
        });
    }

    public static void removeCachedMessage(long entityId) {
        cachedHelpMessages.remove(entityId);
    }

    private static CompletableFuture<List<CommandInfo>> getCommandInfos(long entityId) {
        return TemplateRepository.readAllFuture(entityId).thenApply(templates -> {
            List<CommandInfo> commandInfos = new ArrayList<>();
            for (var command : CommandRegistry.getCommands()) {
                commandInfos.add(new CommandInfo(command.nameWithPrefix(), command.description()));
            }
            for (var template : templates) {
                commandInfos.add(new CommandInfo(Command.PREFIX + template.getCommandName(), template.getDescription()));
            }
            return commandInfos;
        });
    }

    private static List<String> createHelpMessages(Collection<CommandInfo> commandInfo) {
        var commandDescriptions = commandInfo
            .stream()
            .sorted()
            .map(command -> "**" + command.name + "** - " + command.description() + "\n")
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();
        return SplitUtil.split(
            "Commands:\n\n" + commandDescriptions,
            Message.MAX_CONTENT_LENGTH,
            true,
            SplitUtil.Strategy.NEWLINE,
            SplitUtil.Strategy.WHITESPACE,
            SplitUtil.Strategy.ANYWHERE
        );
    }

    private record CommandInfo(String name, String description) implements Comparable<CommandInfo> {

        @Override
        public int compareTo(CommandInfo o) {
            return name.compareTo(o.name);
        }
    }
}
