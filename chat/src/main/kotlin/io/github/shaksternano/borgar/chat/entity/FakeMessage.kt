package io.github.shaksternano.borgar.chat.entity

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.builder.MessageEditBuilder
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.time.OffsetDateTime

data class FakeMessage(
    override val id: String,
    override val content: String,
    private val author: User,
    private val channel: MessageChannel,
    override val timeCreated: OffsetDateTime = OffsetDateTime.now(),
) : Message {

    override val manager: BotManager = author.manager
    override val authorId: String = author.id
    override val referencedMessages: Flow<Message> = emptyFlow()

    override val mentionedUsers: Flow<User> = manager.getMentionedUsers(content)
    override val mentionedChannels: Flow<Channel> = manager.getMentionedChannels(content)
    override val mentionedRoles: Flow<Role> = manager.getMentionedRoles(content)

    override val attachments: List<Attachment> = listOf()
    override val embeds: List<MessageEmbed> = listOf()
    override val customEmojis: Flow<CustomEmoji> = manager.getCustomEmojis(content)
    override val stickers: Flow<Sticker> = emptyFlow()

    override suspend fun getAuthor(): User = author

    override suspend fun getAuthorMember(): Member? = getGuild()?.getMember(author)

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
