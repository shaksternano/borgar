package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;

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
    public void execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        MessageUtil.processMessages(event.getMessage(), message -> {
            if (event.getMessage().equals(message)) {
                List<Member> members = message.getMentions().getMembers();
                if (members.isEmpty()) {
                    return Optional.of(message.getAuthor().getEffectiveAvatarUrl());
                } else {
                    return Optional.of(members.get(0).getEffectiveAvatarUrl());
                }
            } else {
                return Optional.of(message.getAuthor().getEffectiveAvatarUrl());
            }
        }).thenAccept(result -> result.ifPresentOrElse(
            url -> event.getMessage().reply(url + "?size=1024").queue(),
            () -> {
                event.getMessage().reply("Could not find a user to get the profile picture of!").queue();
                Main.getLogger().error("Could not find a user to get the profile picture of, this shouldn't happen!", new IllegalStateException());
            }
        ));
    }
}
