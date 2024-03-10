package io.github.shaksternano.borgar.messaging.interaction.user

import io.github.shaksternano.borgar.messaging.event.UserInteractionEvent
import io.github.shaksternano.borgar.messaging.interaction.InteractionResponse

object UserAvatarInteractionCommand : UserInteractionCommand {

    override val name: String = "Get user avatar"
    override val deferReply: Boolean = false
    override val ephemeralReply: Boolean = true

    override suspend fun respond(event: UserInteractionEvent): InteractionResponse =
        InteractionResponse(
            event.user.effectiveAvatarUrl,
        )
}
