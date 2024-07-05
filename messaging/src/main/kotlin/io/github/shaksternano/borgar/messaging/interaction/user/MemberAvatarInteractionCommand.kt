package io.github.shaksternano.borgar.messaging.interaction.user

import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.messaging.event.UserInteractionEvent
import io.github.shaksternano.borgar.messaging.interaction.InteractionResponse

object MemberAvatarInteractionCommand : UserInteractionCommand {

    override val name: String = "Get user server avatar"
    override val environment: Set<ChannelEnvironment> = setOf(ChannelEnvironment.GUILD)
    override val deferReply: Boolean = false
    override val ephemeralReply: Boolean = true

    override suspend fun respond(event: UserInteractionEvent): InteractionResponse {
        val user = event.user
        val displayedUser = event.getGuild()?.getMember(user) ?: user
        return InteractionResponse(
            displayedUser.effectiveAvatarUrl,
        )
    }
}
