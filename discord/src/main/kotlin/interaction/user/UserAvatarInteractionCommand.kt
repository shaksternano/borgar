package io.github.shaksternano.borgar.discord.interaction.user

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

object UserAvatarInteractionCommand : DiscordUserInteractionCommand {

    override val name: String = "Get user avatar"

    override suspend fun respond(event: UserContextInteractionEvent): Any? {
        val avatarUrl = event.target.effectiveAvatarUrl
        event.reply("$avatarUrl?size=1024")
            .setEphemeral(true)
            .await()
        return null
    }
}
