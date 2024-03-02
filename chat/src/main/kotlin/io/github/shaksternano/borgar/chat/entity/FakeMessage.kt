package io.github.shaksternano.borgar.chat.entity

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.builder.MessageEditBuilder
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import java.time.OffsetDateTime

data class FakeMessage(
    override val id: String,
    override val manager: BotManager,
    override val content: String,
    private val author: User,
    private val channel: MessageChannel,
    override val timeCreated: OffsetDateTime = OffsetDateTime.now(),
) : Message {

    override val referencedMessages: Flow<Message> = emptyFlow()

    private val mentionedUsersSet: Set<User> = manager.getMentionedUsers(content).toSet()
    private val mentionedChannelsSet: Set<Channel> = manager.getMentionedChannels(content).toSet()
    private val mentionedRolesSet: Set<Role> = manager.getMentionedRoles(content).toSet()

    override val mentionedUsers: Flow<User> = mentionedUsersSet.asFlow()
    override val mentionedChannels: Flow<Channel> = mentionedChannelsSet.asFlow()
    override val mentionedRoles: Flow<Role> = mentionedRolesSet.asFlow()

    override val mentionedUserIds: Set<Mentionable> = mentionedUsersSet
    override val mentionedChannelIds: Set<Mentionable> = mentionedChannelsSet
    override val mentionedRoleIds: Set<Mentionable> = mentionedRolesSet

    override val attachments: List<Attachment> = listOf()
    override val embeds: List<MessageEmbed> = listOf()
    override val customEmojis: List<CustomEmoji> = manager.getCustomEmojis(content)
    override val stickers: List<Sticker> = listOf()

    override suspend fun getAuthor(): User = author

    override suspend fun getMember(): Member? = getGuild()?.getMember(author)

    override suspend fun getChannel(): MessageChannel = channel

    override suspend fun getGuild(): Guild? = channel.getGuild()

    override suspend fun edit(block: MessageEditBuilder.() -> Unit): Message {
        val builder = MessageEditBuilder().apply(block)
        return builder.content?.let {
            copy(
                content = it,
                timeCreated = OffsetDateTime.now()
            )
        } ?: this
    }

    override suspend fun delete() = Unit
}
