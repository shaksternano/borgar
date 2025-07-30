package com.shakster.borgar.discord.interaction.modal

import com.shakster.borgar.core.logger
import com.shakster.borgar.discord.command.handleBanned
import com.shakster.borgar.discord.interaction.handleInteractionCommand
import com.shakster.borgar.messaging.command.registerCommands
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

val MODAL_INTERACTION_COMMANDS: Map<String, DiscordModalInteractionCommand> = registerCommands(
    "Discord modal interaction command",
    RunCommandInteractionCommand,
)

suspend fun handleModalInteraction(event: ModalInteractionEvent) {
    handleBanned(event, "modal interaction") {
        return
    }

    val commandName = event.modalId.lowercase()
    val command = MODAL_INTERACTION_COMMANDS[commandName]
    if (command == null) {
        logger.error("Unknown message interaction command: $commandName")
        event.reply("Unknown command!")
            .setEphemeral(true)
            .await()
        return
    }
    handleInteractionCommand(command, event)
}
