package com.shakster.borgar.discord.interaction.user

import com.shakster.borgar.core.logger
import com.shakster.borgar.discord.command.handleBanned
import com.shakster.borgar.discord.interaction.handleInteractionCommand
import com.shakster.borgar.messaging.command.registerCommands
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

val USER_INTERACTION_COMMANDS: Map<String, DiscordUserInteractionCommand> = registerCommands(
    "Discord user interaction command",
    UserAvatarInteractionCommand,
    MemberAvatarInteractionCommand,
    UserBannerInteractionCommand,
)

suspend fun handleUserInteraction(event: UserContextInteractionEvent) {
    handleBanned(event, "user interaction") {
        return
    }

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
