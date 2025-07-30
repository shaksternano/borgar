package com.shakster.borgar.discord.interaction.message

import com.shakster.borgar.core.logger
import com.shakster.borgar.discord.command.handleBanned
import com.shakster.borgar.discord.interaction.handleInteractionCommand
import com.shakster.borgar.messaging.command.registerCommands
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

val MESSAGE_INTERACTION_COMMANDS: Map<String, DiscordMessageInteractionCommand> = registerCommands(
    "Discord message interaction command",
    GifInteractionCommand,
    DownloadInteractionCommand,
    CommandModalInteractionCommand,
    SelectMessageInteractionCommand,
)

suspend fun handleMessageInteraction(event: MessageContextInteractionEvent) {
    handleBanned(event, "message interaction") {
        return
    }

    val commandName = event.name.lowercase()
    val command = MESSAGE_INTERACTION_COMMANDS[commandName]
    if (command == null) {
        logger.error("Unknown message interaction command: $commandName")
        event.reply("Unknown command!")
            .setEphemeral(true)
            .await()
        return
    }
    handleInteractionCommand(command, event)
}
