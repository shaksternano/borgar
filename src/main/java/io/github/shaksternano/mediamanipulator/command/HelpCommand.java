package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.CommandRegistry;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.SplitUtil;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A command that displays the all registered commands.
 */
public class HelpCommand extends BaseCommand {

    /**
     * The command list strings are cached here.
     */
    @Nullable
    private static List<String> cachedHelpMessages = null;

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
    public CompletableFuture<List<MessageCreateData>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        return CompletableFuture.completedFuture(getHelpMessages());
    }

    /**
     * Gets the messages to be displayed when this command is run.
     *
     * @return The messages to be displayed when this command is run.
     */
    public static List<MessageCreateData> getHelpMessages() {
        if (cachedHelpMessages == null) {
            cachedHelpMessages = createHelpMessages();
        }
        return cachedHelpMessages
            .stream()
            .map(MessageCreateData::fromContent)
            .toList();
    }

    private static List<String> createHelpMessages() {
        var commandDescriptions = CommandRegistry.getCommands()
            .stream()
            .sorted()
            .map(command -> "**" + command.getNameWithPrefix() + "** - " + command.getDescription() + "\n")
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
}
