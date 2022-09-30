package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class HaemaCommand extends BaseCommand {

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
    public void execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        event.getMessage().reply("https://media.discordapp.net/attachments/811250178651717642/998714832851312641/impacted.gif\n\nhttps://www.curseforge.com/minecraft/mc-mods/haema").queue();
    }
}
