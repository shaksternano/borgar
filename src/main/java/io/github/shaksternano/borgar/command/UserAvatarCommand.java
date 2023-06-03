package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.Main;
import io.github.shaksternano.borgar.util.MessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserAvatarCommand extends BaseCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public UserAvatarCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public CompletableFuture<List<MessageCreateData>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        var triggerMessage = event.getMessage();
        return MessageUtil.processMessages(triggerMessage, message -> getUserAvatarUrl(triggerMessage, message))
            .thenApply(urlOptional ->
                MessageUtil.createResponse(urlOptional.map(MessageUtil::enlargeImageUrl)
                    .orElseGet(() -> {
                        Main.getLogger().error("Could not find a user to get the profile picture of, this shouldn't happen");
                        return "Could not find a user to get the profile picture of!";
                    })
                )
            );
    }

    private static Optional<String> getUserAvatarUrl(Message triggerMessage, Message message) {
        String avatarUrl;
        if (triggerMessage.equals(message)) {
            var members = message.getMentions().getMembers();
            if (members.isEmpty()) {
                avatarUrl = message.getAuthor().getEffectiveAvatarUrl();
            } else {
                avatarUrl = members.get(0).getEffectiveAvatarUrl();
            }
        } else {
            avatarUrl = message.getAuthor().getEffectiveAvatarUrl();
        }
        return Optional.of(avatarUrl);
    }
}
