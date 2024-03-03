package io.github.shaksternano.borgar.chat.event

import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.chat.interaction.MessageInteractionResponse

interface MessageInteractionEvent : Event {

    val name: String
    val message: Message
    val channel: MessageChannel
    val guild: Guild?

    suspend fun deferReply(ephemeral: Boolean)

    suspend fun reply(response: MessageInteractionResponse): Message

    suspend fun reply(content: String): Message = reply(MessageInteractionResponse(content))
}
