package io.github.shaksternano.borgar.listener;

import io.github.shaksternano.borgar.command.HelpCommand;
import io.github.shaksternano.borgar.command.util.CommandParser;
import io.github.shaksternano.borgar.command.util.Commands;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
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
        User author = event.getAuthor();
        if (!author.equals(event.getJDA().getSelfUser())) {
            Message message = event.getMessage();
            if (author.getName().equals("74") && message.getContentRaw().contains("timetable")) {
                String emoji = "🤓";
                message.addReaction(Emoji.fromUnicode(emoji)).queue();
                message.reply(emoji).queue();
            }

            CommandParser.parseAndExecute(event);
        }
    }
}
