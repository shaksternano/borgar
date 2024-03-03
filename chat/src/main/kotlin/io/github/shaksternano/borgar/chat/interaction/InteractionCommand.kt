package io.github.shaksternano.borgar.chat.interaction

import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.event.InteractionEvent
import io.github.shaksternano.borgar.core.util.Named

interface InteractionCommand<T : InteractionEvent> : Named {

    val guildOnly: Boolean
        get() = false
    val deferReply: Boolean
    val ephemeralReply: Boolean
        get() = false

    suspend fun respond(event: T): InteractionResponse

    suspend fun onResponseSend(
        response: InteractionResponse,
        sent: Message,
        event: T,
    ) = Unit
}
