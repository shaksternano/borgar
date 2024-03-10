package io.github.shaksternano.borgar.messaging.event

import io.github.shaksternano.borgar.messaging.entity.Message
import io.github.shaksternano.borgar.messaging.entity.channel.MessageChannel

interface MessageInteractionEvent : InteractionEvent {

    val message: Message

    override suspend fun getChannel(): MessageChannel
}
