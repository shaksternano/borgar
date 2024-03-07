package io.github.shaksternano.borgar.chat.entity

import io.github.shaksternano.borgar.chat.builder.MessageCreateBuilder
import io.github.shaksternano.borgar.chat.builder.MessageEditBuilder
import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import kotlinx.coroutines.flow.Flow

interface Message : CommandMessageIntersection, TimeStamped {

    val mentionedUsers: Flow<User>
    val mentionedChannels: Flow<Channel>
    val mentionedRoles: Flow<Role>

    suspend fun reply(content: String): Message = reply {
        this.content = content
    }

    suspend fun reply(block: MessageCreateBuilder.() -> Unit): Message = getChannel().createMessage {
        block()
        referencedMessageIds.add(id)
    }

    suspend fun edit(content: String): Message = edit {
        this.content = content
    }

    suspend fun edit(block: MessageEditBuilder.() -> Unit): Message

    suspend fun delete()
}
