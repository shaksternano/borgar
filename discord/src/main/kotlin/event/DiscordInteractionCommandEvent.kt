package io.github.shaksternano.borgar.discord.event

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.MessageCreateBuilder
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.entity.*
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
import io.github.shaksternano.borgar.discord.toFileUpload
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.command.CommandArguments
import io.github.shaksternano.borgar.messaging.command.CommandMessageIntersection
import io.github.shaksternano.borgar.messaging.command.CommandResponse
import io.github.shaksternano.borgar.messaging.command.getDefaultStringOrEmpty
import io.github.shaksternano.borgar.messaging.entity.*
import io.github.shaksternano.borgar.messaging.entity.channel.Channel
import io.github.shaksternano.borgar.messaging.entity.channel.MessageChannel
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import net.dv8tion.jda.api.entities.channel.concrete.GroupChannel
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.OffsetDateTime

class DiscordInteractionCommandEvent<T>(
    private val discordEvent: T,
    discordChannel: net.dv8tion.jda.api.entities.channel.middleman.MessageChannel,
    private val attachments: List<Attachment> = emptyList(),
    referencedMessage: Message? = null,
) : CommandEvent where T : Interaction, T : IReplyCallback {

    override val manager: BotManager = DiscordManager[discordEvent.jda]
    override val id: String = discordEvent.id
    override val authorId: String = discordEvent.user.id
    override val channelId: String = discordChannel.id
    override val timeCreated: OffsetDateTime = discordEvent.timeCreated
    override val referencedMessages: Flow<Message> =
        referencedMessage?.let { flowOf(it) } ?: emptyFlow()

    private val user: User = DiscordUser(discordEvent.user)
    private val member: Member? = discordEvent.member?.let { DiscordMember(it) }
    private val channel: MessageChannel = DiscordMessageChannel(
        discordChannel,
        discordEvent.context,
    )
    private val guild: Guild? = discordEvent.guild?.let { DiscordGuild(it) }
    private val group: Group? = run {
        if (discordChannel is GroupChannel) {
            DiscordGroup(discordChannel)
        } else {
            null
        }
    }

    override var ephemeralReply: Boolean = false
    private var deferReply: Boolean = false
    private var replied: Boolean = false

    override suspend fun getAuthor(): User = user

    override suspend fun getAuthorMember(): Member? = member

    override suspend fun getChannel(): MessageChannel = channel

    override suspend fun getEnvironment(): ChannelEnvironment = channel.environment

    override suspend fun getGuild(): Guild? = guild

    override suspend fun getGroup(): Group? = group

    override suspend fun deferReply() {
        discordEvent.deferReply(ephemeralReply).await()
        deferReply = true
    }

    override suspend fun reply(response: CommandResponse): Message {
        val message = MessageCreateBuilder(
            content = response.content,
            files = response.files.map(DataSource::toFileUpload),
        ).build()
        return if (replied) {
            val discordResponseMessage = discordEvent.hook.sendMessage(message)
                .setEphemeral(ephemeralReply)
                .setSuppressEmbeds(response.suppressEmbeds)
                .await()
            DiscordMessage(discordResponseMessage)
        } else {
            replied = true
            discordEvent.reply(
                message,
                deferReply,
                ephemeralReply,
                response.suppressEmbeds,
            )
        }
    }

    override fun asMessageIntersection(arguments: CommandArguments): CommandMessageIntersection =
        object : CommandMessageIntersection {
            override val id: String = this@DiscordInteractionCommandEvent.id
            override val name: String? = (discordEvent as? CommandInteractionPayload)?.name
            override val authorId: String = this@DiscordInteractionCommandEvent.authorId
            override val channelId: String = this@DiscordInteractionCommandEvent.channelId
            override val manager: BotManager = this@DiscordInteractionCommandEvent.manager
            override val content: String = arguments.getDefaultStringOrEmpty()
            override val attachments: List<Attachment> = this@DiscordInteractionCommandEvent.attachments
            override val customEmojis: Flow<CustomEmoji> = manager.getCustomEmojis(content)
            override val stickers: Flow<Sticker> = emptyFlow()
            override val referencedMessages: Flow<Message> = this@DiscordInteractionCommandEvent.referencedMessages
            override val mentionedUsers: Flow<User> = emptyFlow()
            override val mentionedChannels: Flow<Channel> = emptyFlow()
            override val mentionedRoles: Flow<Role> = emptyFlow()

            override suspend fun getAuthor(): User = this@DiscordInteractionCommandEvent.getAuthor()

            override suspend fun getAuthorMember(): Member? = this@DiscordInteractionCommandEvent.getAuthorMember()

            override suspend fun getChannel(): MessageChannel = this@DiscordInteractionCommandEvent.getChannel()

            override suspend fun getGuild(): Guild? = this@DiscordInteractionCommandEvent.getGuild()

            override suspend fun getGroup(): Group? = this@DiscordInteractionCommandEvent.getGroup()

            override suspend fun getEmbeds(): List<MessageEmbed> = emptyList()
        }
}

suspend fun IReplyCallback.reply(
    message: MessageCreateData,
    deferReply: Boolean,
    ephemeralReply: Boolean,
    suppressEmbeds: Boolean,
): Message {
    val discordResponse = if (deferReply) {
        hook.sendMessage(message)
            .setEphemeral(ephemeralReply)
            .setSuppressEmbeds(suppressEmbeds)
    } else {
        reply(message)
            .setEphemeral(ephemeralReply)
            .setSuppressEmbeds(suppressEmbeds)
            .await()
            .retrieveOriginal()
    }.await()
    return DiscordMessage(discordResponse)
}
