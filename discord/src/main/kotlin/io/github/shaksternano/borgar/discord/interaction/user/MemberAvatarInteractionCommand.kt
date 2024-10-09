package io.github.shaksternano.borgar.discord.interaction.user

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType

object MemberAvatarInteractionCommand : DiscordUserInteractionCommand {

    override val name: String = "Get user server avatar"
    override val environment: Set<InteractionContextType> = setOf(InteractionContextType.GUILD)

    override suspend fun respond(event: UserContextInteractionEvent): Any? {
        val user = event.target
        val avatarUrl = event.guild?.getMember(user)?.effectiveAvatarUrl ?: user.effectiveAvatarUrl
        event.reply(avatarUrl)
            .setEphemeral(true)
            .await()
        return null
    }
}
