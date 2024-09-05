package io.github.shaksternano.borgar.messaging.interaction.message

import io.github.shaksternano.borgar.messaging.event.MessageInteractionEvent
import io.github.shaksternano.borgar.messaging.interaction.InteractionResponse
import io.github.shaksternano.borgar.messaging.util.setSelectedMessage

object SelectMessageCommand : MessageInteractionCommand {

    override val name: String = "Select Message"
    override val deferReply: Boolean = false
    override val ephemeralReply: Boolean = true

    override suspend fun respond(event: MessageInteractionEvent): InteractionResponse {
        val message = event.message
        setSelectedMessage(
            userId = event.getAuthor().id,
            channelId = event.getChannel().id,
            platform = event.manager.platform,
            message = message,
        )
        return InteractionResponse(
            "The message ${message.link} has been selected for your next command."
        )
    }
}
