package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.await
import io.github.shaksternano.borgar.discord.entity.channel.DiscordChannel
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
import io.github.shaksternano.borgar.discord.toFileUpload
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.builder.MessageEditBuilder
import io.github.shaksternano.borgar.messaging.entity.*
import io.github.shaksternano.borgar.messaging.entity.channel.Channel
import io.github.shaksternano.borgar.messaging.entity.channel.MessageChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import net.dv8tion.jda.api.utils.messages.MessageEditData
import java.time.OffsetDateTime

data class DiscordMessage(
    private val discordMessage: net.dv8tion.jda.api.entities.Message,
) : Message, BaseEntity() {

    override val manager: BotManager = DiscordManager[discordMessage.jda]
    override val id: String = discordMessage.id
    override val authorId: String = discordMessage.author.id
    override val timeCreated: OffsetDateTime = discordMessage.timeCreated
    override val content: String = discordMessage.contentRaw
    override val attachments: List<Attachment> = discordMessage.attachments.map { it.convert() }
    override val embeds: List<MessageEmbed> = discordMessage.embeds.map { it.convert() }
    override val customEmojis: Flow<CustomEmoji> = discordMessage.mentions
        .customEmojis
        .map { DiscordCustomEmoji(it, discordMessage.jda) }
        .asFlow()
    override val stickers: Flow<Sticker> = discordMessage.stickers
        .map { DiscordSticker(it, discordMessage.jda) }
        .asFlow()
    override val referencedMessages: Flow<Message> = discordMessage.referencedMessage
        ?.let { flowOf(DiscordMessage(it)) }
        ?: emptyFlow()

    private val author: User = DiscordUser(discordMessage.author)
    private val member: Member? = discordMessage.member?.let { DiscordMember(it) }
    private val channel: MessageChannel = DiscordMessageChannel(discordMessage.channel)
    private val guild: Guild? = if (discordMessage.isFromGuild) {
        DiscordGuild(discordMessage.guild)
    } else {
        null
    }

    override val mentionedUsers: Flow<User> = discordMessage.mentions
        .users
        .map { DiscordUser(it) }
        .asFlow()
    override val mentionedChannels: Flow<Channel> = discordMessage.mentions
        .channels
        .map { DiscordChannel.create(it) }
        .asFlow()
    override val mentionedRoles: Flow<Role> = discordMessage.mentions
        .roles
        .map { DiscordRole(it) }
        .asFlow()

    override suspend fun getAuthor(): User = author

    override suspend fun getAuthorMember(): Member? = member

    override suspend fun getChannel(): MessageChannel = channel

    override suspend fun getGuild(): Guild? = guild

    override suspend fun getGroup(): Group? = null

    override suspend fun edit(block: MessageEditBuilder.() -> Unit): Message {
        val builder = MessageEditBuilder().apply(block)
        val editRequest = builder.convert()
        val editedMessage = discordMessage.editMessage(editRequest).await()
        return DiscordMessage(editedMessage)
    }

    override suspend fun delete() {
        discordMessage.delete().await()
    }

    private fun net.dv8tion.jda.api.entities.Message.Attachment.convert(): Attachment = Attachment(
        id,
        url,
        proxyUrl,
        fileName,
        manager,
    )

    private fun net.dv8tion.jda.api.entities.MessageEmbed.convert(): MessageEmbed = MessageEmbed(
        url = url,
        image = image?.let {
            MessageEmbed.ImageInfo(
                it.url,
                it.proxyUrl,
            )
        },
        video = videoInfo?.let {
            MessageEmbed.VideoInfo(
                it.url,
                it.proxyUrl,
            )
        },
        thumbnail = thumbnail?.let {
            MessageEmbed.ThumbnailInfo(
                it.url,
                it.proxyUrl,
            )
        },
    )

    private fun MessageEditBuilder.convert(): MessageEditData {
        val builder = net.dv8tion.jda.api.utils.messages.MessageEditBuilder()
        content?.let { builder.setContent(it) }
        files?.let { builder.setFiles(it.map(DataSource::toFileUpload)) }
        return builder.build()
    }
}
