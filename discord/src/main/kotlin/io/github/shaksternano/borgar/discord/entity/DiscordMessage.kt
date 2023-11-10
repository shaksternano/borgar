package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.entity.channel.DiscordChannel
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

data class DiscordMessage(
    private val discordMessage: net.dv8tion.jda.api.entities.Message,
) : BaseEntity(), Message {

    override val id: String = discordMessage.id
    override val manager: BotManager = DiscordManager.get(discordMessage.jda)
    override val content: String = discordMessage.contentRaw
    override val attachments: List<Attachment> = discordMessage.attachments.map { it.convert() }
    override val embeds: List<MessageEmbed> = discordMessage.embeds.map { it.convert() }

    private val author: User = DiscordUser(discordMessage.author)
    private val channel: MessageChannel = DiscordMessageChannel(discordMessage.channel)
    private val guild: Guild? = if (discordMessage.isFromGuild) {
        DiscordGuild(discordMessage.guild)
    } else {
        null
    }
    private val referencedMessage: Message? = discordMessage.referencedMessage
        ?.let { DiscordMessage(it) }

    private val mentionedUsersSet: Set<User> = discordMessage.mentions
        .users
        .map { DiscordUser(it) }
        .toSet()
    private val mentionedChannelsSet: Set<Channel> = discordMessage.mentions
        .channels
        .map { DiscordChannel.create(it) }
        .toSet()
    private val mentionedRolesSet: Set<Role> = discordMessage.mentions
        .roles
        .map { DiscordRole(it) }
        .toSet()

    override val mentionedUsers: Flow<User> = mentionedUsersSet.asFlow()
    override val mentionedChannels: Flow<Channel> = mentionedChannelsSet.asFlow()
    override val mentionedRoles: Flow<Role> = mentionedRolesSet.asFlow()

    override val mentionedUserIds: Set<Mentionable> = mentionedUsersSet
    override val mentionedChannelIds: Set<Mentionable> = mentionedChannelsSet
    override val mentionedRoleIds: Set<Mentionable> = mentionedRolesSet

    override val customEmojis: List<CustomEmoji> = discordMessage.mentions
        .customEmojis
        .map { DiscordCustomEmoji(it, discordMessage.jda) }

    override suspend fun getAuthor(): User = author

    override suspend fun getChannel(): MessageChannel = channel

    override suspend fun getGuild(): Guild? = guild

    override suspend fun getReferencedMessage(): Message? = referencedMessage

    private fun net.dv8tion.jda.api.entities.Message.Attachment.convert(): Attachment {
        return Attachment(
            id,
            url,
            proxyUrl,
            fileName,
            manager,
        )
    }

    private fun net.dv8tion.jda.api.entities.MessageEmbed.convert(): MessageEmbed {
        return MessageEmbed(
            image?.let {
                MessageEmbed.ImageInfo(
                    it.url,
                )
            },
            videoInfo?.let {
                MessageEmbed.VideoInfo(
                    it.url,
                )
            },
        )
    }
}
