package io.github.shaksternano.borgar.discord.interaction.user

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.discord.interaction.handleInteractionCommand
import io.github.shaksternano.borgar.messaging.command.registerCommands
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

val USER_INTERACTION_COMMANDS: Map<String, DiscordUserInteractionCommand> = registerCommands(
    "Discord user interaction command",
    UserAvatarInteractionCommand,
    MemberAvatarInteractionCommand,
    UserBannerInteractionCommand,
)

suspend fun handleUserInteraction(event: UserContextInteractionEvent) {
    val commandName = event.name.lowercase()
    val command = USER_INTERACTION_COMMANDS[commandName]
    if (command == null) {
        logger.error("Unknown user interaction command: $commandName")
        event.reply("Unknown command!")
            .setEphemeral(true)
            .await()
        return
    }
    handleInteractionCommand(command, event)
}
