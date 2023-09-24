package io.github.shaksternano.borgar.chat.entity

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
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

    fun asCommandIntersection(): CommandMessageIntersection = object : CommandMessageIntersection {
        override val id: String = this@Message.id
        override val manager: BotManager = this@Message.manager
        override val content: String = this@Message.content
        override val attachments: List<Attachment> = this@Message.attachments
        override val embeds: List<MessageEmbed> = this@Message.embeds

        override suspend fun getUser(): User = getAuthor()

        override suspend fun getChannel(): MessageChannel = this@Message.getChannel()

        override suspend fun getGuild(): Guild? = this@Message.getGuild()

        override suspend fun getReferencedMessage(): Message? = this@Message.getReferencedMessage()
    }
}
