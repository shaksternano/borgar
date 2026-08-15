package com.shakster.borgar.messaging.entity.channel

import com.shakster.borgar.messaging.builder.MessageCreateBuilder
import com.shakster.borgar.messaging.entity.FileUpload
import com.shakster.borgar.messaging.entity.Message
import kotlinx.coroutines.flow.Flow

interface MessageChannel : Channel {

    val cancellableTyping: Boolean

    suspend fun sendTyping()

    suspend fun stopTyping()

    suspend fun createMessage(content: String): Message {
        return createMessage {
            this.content = content
        }
    }

    suspend fun createMessage(vararg files: FileUpload): Message {
        return createMessage {
            this.files.addAll(files)
        }
    }

    suspend fun createMessage(block: MessageCreateBuilder.() -> Unit): Message

    fun getPreviousMessages(beforeId: String): Flow<Message>
}
