package io.github.shaksternano.borgar.chat.entity.channel

import io.github.shaksternano.borgar.chat.builder.MessageCreateBuilder
import io.github.shaksternano.borgar.chat.entity.Message
import kotlinx.coroutines.flow.Flow

interface MessageChannel : Channel {

    suspend fun sendTyping()

    suspend fun stopTyping()

    suspend fun createMessage(content: String): Message = createMessage {
        this.content = content
    }

    suspend fun createMessage(block: MessageCreateBuilder.() -> Unit): Message

    fun getPreviousMessages(beforeId: String): Flow<Message>

    fun getPreviousMessages(before: Message): Flow<Message> =
        getPreviousMessages(before.id)
}
