package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * Gets the image of an emoji.
 */
public class EmojiImageCommand extends BaseCommand {

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
     */
    @Override
    public void execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        MessageUtil.processMessages(event.getMessage(), MessageUtil::getFirstEmojiUrl).ifPresentOrElse(
            url -> event.getMessage().reply(url).queue(),
            () -> event.getMessage().reply("No emoji found!").queue()
        );
    }
}
