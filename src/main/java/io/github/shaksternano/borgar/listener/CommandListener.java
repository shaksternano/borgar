package io.github.shaksternano.borgar.listener;

import io.github.shaksternano.borgar.command.HelpCommand;
import io.github.shaksternano.borgar.command.util.CommandParser;
import io.github.shaksternano.borgar.command.util.Commands;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
            CompletableFuture<?> future = CompletableFuture.completedFuture(null);
            var messages = HelpCommand.getHelpMessages();
            for (var i = 0; i < messages.size(); i++) {
                var message = messages.get(i);
                var isFirst = i == 0;
                future = future.thenCompose(unused -> {
                    // The identity function mapping is needed to make the compiler happy.
                    if (isFirst) {
                        return event.reply(message)
                            .map(Function.identity())
                            .submit()
                            .thenApply(Function.identity());
                    } else {
                        return event.getChannel()
                            .sendMessage(message)
                            .submit()
                            .thenApply(Function.identity());
                    }
                });
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
        var author = event.getAuthor();
        if (!event.getJDA().getSelfUser().equals(author)) {
            var message = event.getMessage();
            if (author.getName().equals("74") && message.getContentRaw().contains("timetable")) {
                var emoji = "🤓";
                message.addReaction(Emoji.fromUnicode(emoji)).queue();
                message.reply(emoji).queue();
            }
            CommandParser.parseAndExecute(event);
        }
    }
}
