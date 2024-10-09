package io.github.shaksternano.borgar.discord.interaction.modal

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.discord.interaction.handleInteractionCommand
import io.github.shaksternano.borgar.messaging.command.registerCommands
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

val MODAL_INTERACTION_COMMANDS: Map<String, DiscordModalInteractionCommand> = registerCommands(
    "Discord modal interaction command",
    RunCommandInteractionCommand,
)

suspend fun handleModalInteraction(event: ModalInteractionEvent) {
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
