package io.github.shaksternano.borgar.chat.event

import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Member
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.chat.interaction.InteractionResponse

interface InteractionEvent : Event {

    val id: String
    val name: String
    var ephemeralReply: Boolean

    suspend fun getAuthor(): User

    suspend fun getAuthorMember(): Member?

    suspend fun getChannel(): Channel?

    suspend fun getGuild(): Guild?

    suspend fun deferReply()

    suspend fun reply(response: InteractionResponse): Message

    suspend fun reply(content: String): Message = reply(InteractionResponse(content))
}
