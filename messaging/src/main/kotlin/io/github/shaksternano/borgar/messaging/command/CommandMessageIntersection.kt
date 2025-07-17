package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.messaging.entity.*
import io.github.shaksternano.borgar.messaging.entity.channel.Channel
import io.github.shaksternano.borgar.messaging.entity.channel.MessageChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface CommandMessageIntersection : Entity {

    val authorId: String
    val channelId: String
    val content: String
    val attachments: List<Attachment>
    val customEmojis: Flow<CustomEmoji>
    val stickers: Flow<Sticker>
    val referencedMessages: Flow<Message>
    val mentionedUsers: Flow<User>
    val mentionedChannels: Flow<Channel>
    val mentionedRoles: Flow<Role>

    suspend fun getAuthor(): User

    suspend fun getAuthorMember(): Member?

    suspend fun getChannel(): MessageChannel?

    suspend fun getGuild(): Guild?

    suspend fun getGroup(): Group?

    suspend fun getPreviousMessages(): Flow<Message> =
        getChannel()?.getPreviousMessages(id) ?: emptyFlow()

    suspend fun getEmbeds(): List<MessageEmbed>
}
