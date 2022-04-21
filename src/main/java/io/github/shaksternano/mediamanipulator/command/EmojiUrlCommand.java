package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;

/**
 * Gets the image of an emoji.
 */
public class EmojiUrlCommand extends Command {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link CommandParser#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    protected EmojiUrlCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Sends the image URL of the first emoji found in the message, the message it's replying to, or a previously sent message.
     * Only works with custom emojis.
     *
     * @param arguments The arguments of the command.
     * @param event     The {@link MessageReceivedEvent} that triggered the command.
     */
    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) {
        MessageUtil.processMessages(event.getMessage(), message -> {
            List<Emote> emotes = message.getEmotes();
            if (emotes.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(emotes.get(0));
            }
        }).ifPresentOrElse(
                emote -> event.getMessage().reply(emote.getImageUrl()).queue(),
                () -> event.getMessage().reply("No custom emoji found!").queue()
        );
    }
}
