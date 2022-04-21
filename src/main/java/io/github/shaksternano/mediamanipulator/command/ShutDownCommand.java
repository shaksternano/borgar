package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * A command that shuts down the bot.
 */
public class ShutDownCommand extends Command {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link CommandParser#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    protected ShutDownCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Shuts down the bot if the user that triggered the command has the required permissions.
     *
     * @param arguments The arguments of the command.
     * @param event     The {@link MessageReceivedEvent} that triggered the command.
     */
    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        long userId = userMessage.getAuthor().getIdLong();

        if (userId == Main.getOwnerId()) {
            userMessage.reply("Shutting down!").queue(message -> {
                Main.LOGGER.info("Shut down request received from user " + userId + ", shutting down!");
                Main.shutdown();
            });
        } else {
            userMessage.reply("You are not authorised to use this command!").queue();
        }
    }
}
