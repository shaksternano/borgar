package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.entities.MessageSticker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;

public class StickerImageCommand extends BaseCommand {

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
    public void execute(String[] arguments, MessageReceivedEvent event) {
        MessageUtil.processMessages(event.getMessage(), message -> {
            List<MessageSticker> stickers = message.getStickers();
            if (stickers.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(stickers.get(0).getIconUrl());
            }
        }).ifPresentOrElse(
                url -> event.getMessage().reply(url).queue(),
                () -> event.getMessage().reply("No sticker found!").queue()
        );
    }
}
