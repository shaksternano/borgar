package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class HaemaCommand extends SimpleCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public HaemaCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected String response(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        return """
            https://media.discordapp.net/attachments/964551969509347331/1134818935829712987/YOU_SHOULD_DOWNLOAD_HAEMA_NOW.gif

            https://www.curseforge.com/minecraft/mc-mods/haema
            """;
    }
}
