package io.github.shaksternano.mediamanipulator.listener;

import io.github.shaksternano.mediamanipulator.command.HelpCommand;
import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.command.util.Commands;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listens for commands in Discord messages.
 */
public class CommandListener extends ListenerAdapter {

    /**
     * Listens for slash commands.
     *
     * @param event the {@link SlashCommandInteractionEvent} that triggered the listener.
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals(Commands.HELP.getName())) {
            List<MessageCreateData> messages = HelpCommand.getHelpMessages();
            for (int i = 0; i < messages.size(); i++) {
                MessageCreateData message = messages.get(i);
                if (i == 0) {
                    event.reply(message).queue();
                } else {
                    event.getChannel().sendMessage(message).queue();
                }
            }
        }
    }

    /**
     * Listens for prefix commands.
     *
     * @param event the {@link MessageReceivedEvent} that triggered the listener.
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().equals(event.getJDA().getSelfUser())) {
            CommandParser.parseAndExecute(event);
        }
    }
}
