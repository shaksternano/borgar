package io.github.shaksternano.borgar.discord.interaction.user

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.discord.entity.getBannerUrl
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

object UserBannerInteractionCommand : DiscordUserInteractionCommand {

    override val name: String = "Get user banner"

    override suspend fun respond(event: UserContextInteractionEvent): Any? {
        val user = event.target
        event.reply(event.target.getBannerUrl() ?: "${user.asMention} has no banner.")
            .setEphemeral(true)
            .await()
        return null
    }
}
