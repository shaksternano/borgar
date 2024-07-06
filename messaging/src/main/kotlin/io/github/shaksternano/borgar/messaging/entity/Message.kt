package io.github.shaksternano.borgar.messaging.entity

import io.github.shaksternano.borgar.messaging.builder.MessageCreateBuilder
import io.github.shaksternano.borgar.messaging.builder.MessageEditBuilder
import io.github.shaksternano.borgar.messaging.command.CommandMessageIntersection

interface Message : CommandMessageIntersection, TimeStamped {

    val link: String

    suspend fun reply(content: String): Message = reply {
        this.content = content
    }

    suspend fun reply(block: MessageCreateBuilder.() -> Unit): Message = getChannel()?.createMessage {
        block()
        referencedMessageIds.clear()
        referencedMessageIds.add(id)
    } ?: error("Message channel not found")

    suspend fun edit(content: String): Message = edit {
        this.content = content
    }

    suspend fun edit(block: MessageEditBuilder.() -> Unit): Message

    suspend fun delete()
}
