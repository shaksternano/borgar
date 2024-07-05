package io.github.shaksternano.borgar.messaging.interaction

import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.core.util.Named
import io.github.shaksternano.borgar.messaging.entity.Message
import io.github.shaksternano.borgar.messaging.event.InteractionEvent

interface InteractionCommand<T : InteractionEvent> : Named {

    val environment: Set<ChannelEnvironment>
        get() = ChannelEnvironment.ALL
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
