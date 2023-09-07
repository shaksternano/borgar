package io.github.shaksternano.borgar.core.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.core.command.util.CommandResponse;
import io.github.shaksternano.borgar.core.util.MessageUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Gets the image of an emoji.
 */
public class EmojiImageCommand extends BaseCommand<Void> {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public EmojiImageCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Sends the image URL of the first emoji found in the message, the message it's replying to, or a previously sent message.
     *
     * @param arguments      The arguments of the command.
     * @param extraArguments A multimap mapping the additional parameter names to a list of the arguments.
     * @param event          The {@link MessageReceivedEvent} that triggered the command.
     * @return A {@code CompletableFuture} that completes with a list of {@code MessageCreateData} that will be sent to the channel where the command was triggered.
     */
    @Override
    public CompletableFuture<CommandResponse<Void>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        return MessageUtil.processMessages(event.getMessage(), MessageUtil::getFirstEmojiUrl)
            .thenApply(urlOptional ->
                new CommandResponse<>(urlOptional.orElse("No emoji found!"))
            );
    }
}
