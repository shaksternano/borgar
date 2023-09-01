package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.command.util.CommandResponse;
import io.github.shaksternano.borgar.util.MessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class StickerImageCommand extends BaseCommand<Void> {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public StickerImageCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public CompletableFuture<CommandResponse<Void>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        return MessageUtil.processMessages(event.getMessage(), StickerImageCommand::getFirstStickerUrl)
            .thenApply(urlOptional ->
                new CommandResponse<>(urlOptional.orElse("No sticker found!"))
            );
    }

    private static Optional<String> getFirstStickerUrl(Message message) {
        return message.getStickers()
            .stream()
            .map(StickerItem::getIconUrl)
            .findFirst();
    }
}
