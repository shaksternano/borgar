package io.github.shaksternano.borgar.messaging.event

import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.messaging.command.CommandArguments
import io.github.shaksternano.borgar.messaging.command.CommandMessageIntersection
import io.github.shaksternano.borgar.messaging.command.CommandResponse
import io.github.shaksternano.borgar.messaging.entity.*
import io.github.shaksternano.borgar.messaging.entity.channel.MessageChannel
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
