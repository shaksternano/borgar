package io.github.shaksternano.borgar.chat.event

import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Member
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.chat.interaction.MessageInteractionResponse

interface MessageInteractionEvent : Event {

    val id: String
    val name: String
    val message: Message

    var ephemeralReply: Boolean

    suspend fun getUser(): User

    suspend fun getMember(): Member?

    suspend fun getChannel(): MessageChannel

    suspend fun getGuild(): Guild?

    suspend fun deferReply()

    suspend fun reply(response: MessageInteractionResponse): Message

    suspend fun reply(content: String): Message = reply(MessageInteractionResponse(content))
}
