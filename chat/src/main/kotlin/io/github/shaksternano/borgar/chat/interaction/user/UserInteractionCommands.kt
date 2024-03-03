package io.github.shaksternano.borgar.chat.interaction.user

import io.github.shaksternano.borgar.chat.command.registerCommands
import io.github.shaksternano.borgar.chat.event.UserInteractionEvent
import io.github.shaksternano.borgar.chat.interaction.message.handleInteractionCommand
import io.github.shaksternano.borgar.core.logger

val USER_INTERACTION_COMMANDS: Map<String, UserInteractionCommand> = registerCommands(
    "user interaction command",
    UserAvatarInteractionCommand,
    UserGuildAvatarInteractionCommand,
)

suspend fun handleUserInteraction(event: UserInteractionEvent) {
    val commandName = event.name
    val command = USER_INTERACTION_COMMANDS[commandName]
    if (command == null) {
        logger.error("No user interaction command with the name ${event.name} found")
        return
    }
    handleInteractionCommand(command, event)
}
