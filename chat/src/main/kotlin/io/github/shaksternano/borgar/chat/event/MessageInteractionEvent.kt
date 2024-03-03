package io.github.shaksternano.borgar.chat.event

import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel

interface MessageInteractionEvent : InteractionEvent {

    val message: Message

    override suspend fun getChannel(): MessageChannel
}
