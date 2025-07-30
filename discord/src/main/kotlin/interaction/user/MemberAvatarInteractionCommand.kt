package com.shakster.borgar.discord.interaction.user

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType

object MemberAvatarInteractionCommand : DiscordUserInteractionCommand {

    override val name: String = "Get user server avatar"
    override val environment: Set<InteractionContextType> = setOf(InteractionContextType.GUILD)

    override suspend fun respond(event: UserContextInteractionEvent): Any? {
        val user = event.target
        val avatarUrl = event.guild
            ?.retrieveMember(user)
            ?.useCache(false)
            ?.await()
            ?.effectiveAvatarUrl
            ?: user.effectiveAvatarUrl
        event.reply("$avatarUrl?size=1024")
            .setEphemeral(true)
            .await()
        return null
    }
}
