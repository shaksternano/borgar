package io.github.shaksternano.borgar.chat.interaction

import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.event.MessageInteractionEvent

interface MessageInteractionCommand {

    val name: String
    val deferReply: Boolean
    val ephemeral: Boolean
        get() = false

    suspend fun respond(event: MessageInteractionEvent): MessageInteractionResponse

    suspend fun onResponseSend(
        response: MessageInteractionResponse,
        sent: Message,
        event: MessageInteractionEvent,
    ) = Unit
}
