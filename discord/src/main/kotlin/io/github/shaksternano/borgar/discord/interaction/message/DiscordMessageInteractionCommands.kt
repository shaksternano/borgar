package io.github.shaksternano.borgar.discord.interaction.message

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.discord.interaction.handleInteractionCommand
import io.github.shaksternano.borgar.messaging.command.registerCommands
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

val MESSAGE_INTERACTION_COMMANDS: Map<String, DiscordMessageInteractionCommand> = registerCommands(
    "Discord message interaction command",
    DownloadInteractionCommand,
    CommandModalInteractionCommand,
    SelectMessageCommand,
)

suspend fun handleMessageInteraction(event: MessageContextInteractionEvent) {
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
