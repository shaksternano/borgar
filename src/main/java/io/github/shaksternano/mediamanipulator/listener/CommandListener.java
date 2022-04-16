package io.github.shaksternano.mediamanipulator.listener;

import io.github.shaksternano.mediamanipulator.command.CommandParser;
import io.github.shaksternano.mediamanipulator.command.HelpCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {

    public static final CommandListener INSTANCE = new CommandListener();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals(HelpCommand.INSTANCE.getName())) {
            event.reply(HelpCommand.getHelpMessage()).queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        CommandParser.parseAndTryExecute(event);
    }
}
