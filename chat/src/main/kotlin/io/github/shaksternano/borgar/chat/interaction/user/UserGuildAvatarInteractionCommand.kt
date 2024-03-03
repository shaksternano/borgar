package io.github.shaksternano.borgar.chat.interaction.user

import io.github.shaksternano.borgar.chat.event.UserInteractionEvent
import io.github.shaksternano.borgar.chat.interaction.InteractionResponse

object UserGuildAvatarInteractionCommand : UserInteractionCommand {

    override val name: String = "Get user server avatar"
    override val guildOnly: Boolean = true
    override val deferReply: Boolean = false
    override val ephemeralReply: Boolean = true

    override suspend fun respond(event: UserInteractionEvent): InteractionResponse {
        val user = event.target
        val displayedUser = event.getGuild()?.getMember(user) ?: user
        return InteractionResponse(
            displayedUser.effectiveAvatarUrl,
        )
    }
}
