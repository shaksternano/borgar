package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ShutDownCommand extends Command {

    protected ShutDownCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        long userId = userMessage.getAuthor().getIdLong();

        if (userId == Main.getOwnerId()) {
            userMessage.reply( "Shutting down!").queue(message -> {
                Main.LOGGER.info("Shut down request received from user " + userId + ", shutting down!");
                message.getJDA().shutdownNow();
                System.exit(0);
            });
        } else {
            userMessage.reply("You are not authorised to use this command!").queue();
        }
    }
}
