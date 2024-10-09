package io.github.shaksternano.borgar.discord.interaction.user

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

object UserAvatarInteractionCommand : DiscordUserInteractionCommand {

    override val name: String = "Get user avatar"

    override suspend fun respond(event: UserContextInteractionEvent): Any? {
        event.reply(event.user.effectiveAvatarUrl)
            .setEphemeral(true)
            .await()
        return null
    }
}
