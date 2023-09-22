package io.github.shaksternano.borgar.chat.event

import io.github.shaksternano.borgar.chat.command.CommandArguments
import io.github.shaksternano.borgar.chat.command.CommandMessageUnion
import io.github.shaksternano.borgar.chat.command.CommandResponse
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Managed
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel

interface CommandEvent : Managed {

    val id: String

    suspend fun getUser(): User

    suspend fun getChannel(): MessageChannel

    suspend fun getGuild(): Guild?

    suspend fun getReferencedMessage(): Message?

    suspend fun respond(response: CommandResponse): Message

    suspend fun respond(content: String): Message = respond(CommandResponse(content))

    fun asMessageUnion(arguments: CommandArguments): CommandMessageUnion
}
