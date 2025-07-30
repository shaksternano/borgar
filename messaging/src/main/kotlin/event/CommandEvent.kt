package com.shakster.borgar.messaging.event

import com.shakster.borgar.core.util.ChannelEnvironment
import com.shakster.borgar.messaging.command.CommandArguments
import com.shakster.borgar.messaging.command.CommandMessageIntersection
import com.shakster.borgar.messaging.command.CommandResponse
import com.shakster.borgar.messaging.entity.*
import com.shakster.borgar.messaging.entity.channel.MessageChannel
import kotlinx.coroutines.flow.Flow

interface CommandEvent : Managed, TimeStamped {

    val id: String
    val authorId: String
    val channelId: String
    val referencedMessages: Flow<Message>
    var ephemeralReply: Boolean

    suspend fun getAuthor(): User

    suspend fun getAuthorMember(): Member?

    suspend fun getChannel(): MessageChannel

    suspend fun getEnvironment(): ChannelEnvironment

    suspend fun getGuild(): Guild?

    suspend fun getGroup(): Group?

    suspend fun deferReply()

    suspend fun reply(response: CommandResponse): Message

    suspend fun reply(content: String): Message = reply(CommandResponse(content))

    fun asMessageIntersection(arguments: CommandArguments): CommandMessageIntersection
}
