package io.github.shaksternano.borgar.chat.event

import io.github.shaksternano.borgar.chat.command.CommandArguments
import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
import io.github.shaksternano.borgar.chat.command.CommandResponse
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel

interface CommandEvent : Managed, TimeStamped {

    val id: String

    suspend fun getAuthor(): User

    suspend fun getChannel(): MessageChannel

    suspend fun getGuild(): Guild?

    suspend fun getReferencedMessage(): Message?

    suspend fun reply(response: CommandResponse): Message

    suspend fun reply(content: String): Message = reply(CommandResponse(content))

    fun asMessageIntersection(arguments: CommandArguments): CommandMessageIntersection
}
