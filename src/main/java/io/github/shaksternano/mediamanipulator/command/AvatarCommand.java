package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;

public class AvatarCommand extends Command {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    protected AvatarCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) {
        MessageUtil.processMessages(event.getMessage(), message -> {
            if (event.getMessage().equals(message)) {
                List<Member> members = message.getMentionedMembers();
                if (!members.isEmpty()) {
                    return Optional.of(members.get(0).getUser().getEffectiveAvatarUrl());
                } else {
                    return Optional.of(event.getAuthor().getEffectiveAvatarUrl());
                }
            } else {
                return Optional.of(event.getAuthor().getEffectiveAvatarUrl());
            }
        }).ifPresentOrElse(
                url -> event.getMessage().reply(url).queue(),
                () -> {
                    event.getMessage().reply("Could not find a user to get the profile picture of!").queue();
                    Main.LOGGER.error("Could not find a user to get the profile picture of, this shouldn't happen!", new IllegalStateException());
                }
        );
    }
}
