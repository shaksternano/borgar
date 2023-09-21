package io.github.shaksternano.borgar.chat.entity

import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import kotlinx.coroutines.flow.Flow

interface Message : Entity {

    val content: String
    val attachments: List<Attachment>
    val embeds: List<MessageEmbed>

    val mentionedUsers: Flow<User>
    val mentionedChannels: Flow<Channel>
    val mentionedRoles: Flow<Role>

    val mentionedUserIds: Set<Mentionable>
    val mentionedChannelIds: Set<Mentionable>
    val mentionedRoleIds: Set<Mentionable>

    suspend fun getAuthor(): User

    suspend fun getChannel(): MessageChannel

    suspend fun getGuild(): Guild?

    suspend fun getReferencedMessage(): Message?
}
