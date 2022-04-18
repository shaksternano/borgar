package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCommand extends Command {

    private static String cachedHelpMessage;

    protected HelpCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        userMessage.reply(getHelpMessage()).queue();
    }

    public static Message getHelpMessage() {
        if (cachedHelpMessage == null) {
            StringBuilder builder = new StringBuilder("Commands:\n");

            CommandRegistry.getCommands().stream().sorted().forEach(
                    command -> builder
                            .append('!')
                            .append(command.getName())
                            .append(" - ")
                            .append(command.getDescription())
                            .append("\n")
            );

            cachedHelpMessage = builder.toString();
        }

        return new MessageBuilder(cachedHelpMessage).build();
    }
}
