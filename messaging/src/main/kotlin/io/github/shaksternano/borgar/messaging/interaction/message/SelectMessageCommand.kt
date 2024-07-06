package io.github.shaksternano.borgar.messaging.interaction.message

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.github.shaksternano.borgar.messaging.MessagingPlatform
import io.github.shaksternano.borgar.messaging.entity.Message
import io.github.shaksternano.borgar.messaging.event.MessageInteractionEvent
import io.github.shaksternano.borgar.messaging.interaction.InteractionResponse
import java.time.Duration

object SelectMessageCommand : MessageInteractionCommand {

    override val name: String = "Select Message"
    override val deferReply: Boolean = false
    override val ephemeralReply: Boolean = true
    private val selectedMessages: Cache<SelectedMessageKey, Message> = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.ofHours(1))
        .build()

    override suspend fun respond(event: MessageInteractionEvent): InteractionResponse {
        val message = event.message
        selectedMessages.put(
            SelectedMessageKey(
                userId = event.getAuthor().id,
                channelId = event.getChannel().id,
                platform = event.manager.platform,
            ),
            message,
        )
        return InteractionResponse(
            "The message ${message.link} has been selected for your next command."
        )
    }

    fun getAndExpireSelectedMessage(
        userId: String,
        channelId: String,
        platform: MessagingPlatform,
    ): Message? {
        val key = SelectedMessageKey(userId, channelId, platform)
        return selectedMessages.getIfPresent(key).also {
            selectedMessages.invalidate(key)
        }
    }

    private data class SelectedMessageKey(
        val userId: String,
        val channelId: String,
        val platform: MessagingPlatform,
    )
}
