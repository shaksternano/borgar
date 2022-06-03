package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ServerBannerCommand extends BaseCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ServerBannerCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public void execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        Message message = event.getMessage();
        String serverBannerUrl = message.getGuild().getBannerUrl();
        message.reply(serverBannerUrl == null ? "No server banner image set!" : serverBannerUrl).queue();
    }
}
