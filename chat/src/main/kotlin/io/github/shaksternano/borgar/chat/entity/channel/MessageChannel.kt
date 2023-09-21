package io.github.shaksternano.borgar.chat.entity.channel

import io.github.shaksternano.borgar.chat.builder.MessageCreateBuilder
import io.github.shaksternano.borgar.chat.entity.Message
import kotlinx.coroutines.flow.Flow

interface MessageChannel : Channel {

    suspend fun sendTyping()

    suspend fun createMessage(content: String): Message {
        return createMessage {
            this.content = content
        }
    }

    suspend fun createMessage(block: MessageCreateBuilder.() -> Unit): Message

    fun getPreviousMessages(beforeId: String, limit: Int): Flow<Message>

    fun getPreviousMessages(before: Message, limit: Int): Flow<Message> =
        getPreviousMessages(before.id, limit)
}
