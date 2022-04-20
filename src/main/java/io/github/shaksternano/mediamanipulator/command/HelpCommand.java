package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * A command that displays the all registered commands.
 */
public class HelpCommand extends Command {

    /**
     * The command list string is cached here.
     */
    private static String cachedHelpMessage;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link CommandParser#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    protected HelpCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Sends the command list in the channel the command was triggered in.
     * @param arguments The arguments of the command.
     * @param event     The {@link MessageReceivedEvent} that triggered the command.
     */
    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        userMessage.reply(getHelpMessage()).queue();
    }

    /**
     * Gets the message to be displayed when this command is run.
     * @return The message to be displayed when this command is run.
     */
    public static Message getHelpMessage() {
        if (cachedHelpMessage == null) {
            StringBuilder builder = new StringBuilder("Commands:\n");

            CommandRegistry.getCommands().stream().sorted().forEach(
                    command -> builder
                            .append(CommandParser.COMMAND_PREFIX)
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
