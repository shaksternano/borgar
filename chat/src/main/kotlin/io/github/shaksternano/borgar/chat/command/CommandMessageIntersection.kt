package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import kotlinx.coroutines.flow.Flow

interface CommandMessageIntersection : Entity {

    val content: String
    val attachments: List<Attachment>
    val embeds: List<MessageEmbed>
    val customEmojis: List<CustomEmoji>
    val stickers: List<Sticker>
    val referencedMessages: Flow<Message>

    suspend fun getAuthor(): User

    suspend fun getChannel(): MessageChannel

    suspend fun getGuild(): Guild?

    suspend fun getPreviousMessages(): Flow<Message> =
        getChannel().getPreviousMessages(id)
}
