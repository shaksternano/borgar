package io.github.shaksternano.borgar.messaging.interaction.user

import io.github.shaksternano.borgar.messaging.event.UserInteractionEvent
import io.github.shaksternano.borgar.messaging.interaction.InteractionResponse

object UserBannerInteractionCommand : UserInteractionCommand {

    override val name: String = "Get user banner"
    override val deferReply: Boolean = false
    override val ephemeralReply: Boolean = true

    override suspend fun respond(event: UserInteractionEvent): InteractionResponse {
        val bannerUrl = event.user.getBannerUrl() ?: return InteractionResponse("User has no banner.")
        return InteractionResponse(bannerUrl)
    }
}
