package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.CommandRegistry;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A command that displays the all registered commands.
 */
public class HelpCommand extends BaseCommand {

    /**
     * The command list strings are cached here.
     */
    private static List<String> cachedHelpMessages = null;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public HelpCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Sends the command list in the channel the command was triggered in.
     *
     * @param arguments      The arguments of the command.
     * @param extraArguments A multimap mapping the additional parameter names to a list of the arguments.
     * @param event          The {@link MessageReceivedEvent} that triggered the command.
     */
    @Override
    public void execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        Message userMessage = event.getMessage();
        List<MessageCreateData> messages = getHelpMessages();
        for (int i = 0; i < messages.size(); i++) {
            MessageCreateData message = messages.get(i);

            if (i == 0) {
                userMessage.reply(message).queue();
            } else {
                event.getChannel().sendMessage(message).queue();
            }
        }
    }

    /**
     * Gets the messages to be displayed when this command is run.
     *
     * @return The messages to be displayed when this command is run.
     */
    public static List<MessageCreateData> getHelpMessages() {
        if (cachedHelpMessages == null) {
            cachedHelpMessages = createHelpMessages();
        }

        return cachedHelpMessages
            .stream()
            .map(message -> new MessageCreateBuilder().addContent(message).build())
            .collect(ImmutableList.toImmutableList());
    }

    private static List<String> createHelpMessages() {
        int maxLength = 2000;

        StringBuilder builder = new StringBuilder("Commands:\n\n");
        int totalLength = builder.length();
        List<String> messages = new ArrayList<>();

        List<Command> commands = new ArrayList<>(CommandRegistry.getCommands());
        commands.sort(Comparator.comparing(Command::getName));

        for (Command command : commands) {
            String commandLine = "**" + command.getNameWithPrefix() + "** - " + command.getDescription() + "\n";
            int length = commandLine.length();
            totalLength += length;

            if (totalLength > maxLength) {
                builder.deleteCharAt(builder.length() - 1);
                messages.add(builder.toString());
                builder = new StringBuilder(commandLine);
                totalLength = length;
            } else {
                builder.append(commandLine);
            }
        }

        messages.add(builder.toString());

        return messages;
    }
}
