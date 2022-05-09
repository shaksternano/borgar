package io.github.shaksternano.mediamanipulator.listener;

import io.github.shaksternano.mediamanipulator.command.CommandParser;
import io.github.shaksternano.mediamanipulator.command.Commands;
import io.github.shaksternano.mediamanipulator.command.HelpCommand;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for commands in Discord messages.
 */
public class CommandListener extends ListenerAdapter {

    /**
     * The instance of this class.
     */
    public static final CommandListener INSTANCE = new CommandListener();

    /**
     * Listens for slash commands.
     *
     * @param event the {@link SlashCommandInteractionEvent} that triggered the listener.
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals(Commands.HELP.getName())) {
            event.reply(HelpCommand.getHelpMessage()).queue();
        }
    }

    /**
     * Listens for prefix commands.
     *
     * @param event the {@link MessageReceivedEvent} that triggered the listener.
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        for (MessageEmbed embed : event.getMessage().getEmbeds()) {
            System.out.println(embed.toData());
            System.out.println("URL: " + embed.getUrl());
            if (embed.getThumbnail() != null) {
                System.out.println("Thumbnail URL: " + embed.getThumbnail().getUrl());
            }
            if (embed.getSiteProvider() != null) {
                System.out.println("Site Provider URL: " + embed.getSiteProvider().getUrl());
            }
            if (embed.getAuthor() != null) {
                System.out.println("Author URL: " + embed.getAuthor().getUrl());
            }
        }

        CommandParser.parseAndExecute(event);
    }
}
