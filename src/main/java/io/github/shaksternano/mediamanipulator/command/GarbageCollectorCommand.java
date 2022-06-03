package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class GarbageCollectorCommand extends BotOwnerCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public GarbageCollectorCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void botOwnerOperation(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        long userId = userMessage.getAuthor().getIdLong();

        userMessage.reply("Running garbage collector!").queue();
        Main.getLogger().info("Garbage collection request received from user " + userId + ", running garbage collector!");
        System.gc();
    }
}
