package io.github.shaksternano.borgar.messaging.entity.channel

import io.github.shaksternano.borgar.messaging.builder.MessageCreateBuilder
import io.github.shaksternano.borgar.messaging.entity.Message
import kotlinx.coroutines.flow.Flow

interface MessageChannel : Channel {

    suspend fun sendTyping()

    suspend fun stopTyping()

    suspend fun createMessage(content: String): Message = createMessage {
        this.content = content
    }

    suspend fun createMessage(block: MessageCreateBuilder.() -> Unit): Message

    fun getPreviousMessages(beforeId: String): Flow<Message>
}
