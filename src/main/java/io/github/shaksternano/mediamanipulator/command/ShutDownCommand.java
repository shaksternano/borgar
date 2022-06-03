package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * A command that shuts down the bot.
 */
public class ShutDownCommand extends BotOwnerCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ShutDownCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Shuts down the bot if the user that triggered the command has the required permissions.
     *
     * @param arguments      The arguments of the command.
     * @param extraArguments A multimap mapping the additional parameter names to a list of the arguments.
     * @param event          The {@link MessageReceivedEvent} that triggered the command.
     */
    @Override
    protected void botOwnerOperation(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        long userId = userMessage.getAuthor().getIdLong();

        try {
            userMessage.reply("Shutting down!").complete();
            Main.getLogger().info("Shut down request received from user " + userId + ", shutting down!");
        } catch (Throwable t) {
            Main.getLogger().error("Error while shutting down!", t);
        }

        Main.shutdown(0);
    }
}
