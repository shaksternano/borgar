package io.github.shaksternano.borgar.messaging.interaction.user

import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.messaging.command.registerCommands
import io.github.shaksternano.borgar.messaging.event.UserInteractionEvent
import io.github.shaksternano.borgar.messaging.interaction.message.handleInteractionCommand

val USER_INTERACTION_COMMANDS: Map<String, UserInteractionCommand> = registerCommands(
    "user interaction command",
    UserAvatarInteractionCommand,
    MemberAvatarInteractionCommand,
    UserBannerInteractionCommand,
)

suspend fun handleUserInteraction(event: UserInteractionEvent) {
    val commandName = event.name
    val command = USER_INTERACTION_COMMANDS[commandName]
    if (command == null) {
        logger.error("Unknown user interaction command: $commandName")
        event.ephemeralReply = true
        event.reply("Unknown command!")
        return
    }
    handleInteractionCommand(command, event)
}
