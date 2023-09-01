package io.github.shaksternano.borgar.listener;

import io.github.shaksternano.borgar.command.HelpCommand;
import io.github.shaksternano.borgar.command.util.CommandParser;
import io.github.shaksternano.borgar.command.util.Commands;
import io.github.shaksternano.borgar.util.CompletableFutureUtil;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
        if (event.getName().equals(Commands.HELP.name())) {
            var entityId = Objects.requireNonNullElseGet(event.getGuild(), event::getUser).getIdLong();
            HelpCommand.getHelpMessages(entityId).thenAccept(responses ->
                CompletableFutureUtil.forEachSequentiallyAsync(
                    responses,
                    (response, index) -> {
                        if (index == 0) {
                            return event.reply(response)
                                .setSuppressEmbeds(true)
                                .submit();
                        } else {
                            return event.getChannel()
                                .sendMessage(response)
                                .setSuppressEmbeds(true)
                                .submit();
                        }
                    }
                )
            );
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
                var emoji = "\uD83E\uDD13";
                message.addReaction(Emoji.fromUnicode(emoji)).queue();
                message.reply(emoji).queue();
            }
            CompletableFuture.runAsync(() -> CommandParser.parseAndExecute(event));
            FavouriteHandler.sendFavouriteFile(event);
        }
    }
}
