package com.shakster.borgar.messaging.entity

import com.shakster.borgar.messaging.builder.MessageCreateBuilder
import com.shakster.borgar.messaging.builder.MessageEditBuilder
import com.shakster.borgar.messaging.command.CommandMessageIntersection

interface Message : CommandMessageIntersection, TimeStamped {

    override val name: String?
        get() = null
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
