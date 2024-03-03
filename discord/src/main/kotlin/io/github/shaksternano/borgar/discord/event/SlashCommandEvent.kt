package io.github.shaksternano.borgar.discord.event

import dev.minn.jda.ktx.messages.MessageCreateBuilder
import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.command.CommandArguments
import io.github.shaksternano.borgar.chat.command.CommandMessageIntersection
import io.github.shaksternano.borgar.chat.command.CommandResponse
import io.github.shaksternano.borgar.chat.command.getDefaultStringOrEmpty
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.await
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import io.github.shaksternano.borgar.discord.entity.DiscordMember
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
import io.github.shaksternano.borgar.discord.toFileUpload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.OffsetDateTime

class SlashCommandEvent(
    private val event: SlashCommandInteractionEvent
) : CommandEvent {

    override val manager: BotManager = DiscordManager[event.jda]
    override val id: String = event.id
    override val timeCreated: OffsetDateTime = event.timeCreated
    override val referencedMessages: Flow<Message> = emptyFlow()

    private val user: User = DiscordUser(event.user)
    private val member: Member? = event.member?.let { DiscordMember(it) }
    private val channel: MessageChannel = DiscordMessageChannel(event.channel)
    private val guild: Guild? = event.guild?.let { DiscordGuild(it) }

    override var ephemeralReply: Boolean = false
    private var deferReply: Boolean = false
    private var replied: Boolean = false

    override suspend fun getAuthor(): User = user

    override suspend fun getMember(): Member? = member

    override suspend fun getChannel(): MessageChannel = channel

    override suspend fun getGuild(): Guild? = guild

    override suspend fun deferReply() {
        event.deferReply(ephemeralReply).await()
        deferReply = true
    }

    override suspend fun reply(response: CommandResponse): Message {
        val message = MessageCreateBuilder(
            content = response.content,
            files = response.files.map(DataSource::toFileUpload),
        ).build()
        return if (replied) {
            val discordResponseMessage = event.channel.sendMessage(message)
                .setSuppressEmbeds(response.suppressEmbeds)
                .await()
            DiscordMessage(discordResponseMessage)
        } else {
            replied = true
            event.reply(
                message,
                channel,
                deferReply,
                ephemeralReply,
                response.suppressEmbeds,
            )
        }
    }

    override fun asMessageIntersection(arguments: CommandArguments): CommandMessageIntersection =
        object : CommandMessageIntersection {
            override val id: String = this@SlashCommandEvent.id
            override val manager: BotManager = this@SlashCommandEvent.manager
            override val content: String = arguments.getDefaultStringOrEmpty()
            override val attachments: List<Attachment> = event.getOptionsByType(OptionType.ATTACHMENT).map {
                val attachment = it.asAttachment
                Attachment(
                    id = attachment.id,
                    url = attachment.url,
                    proxyUrl = attachment.proxyUrl,
                    filename = attachment.fileName,
                    manager = manager,
                    ephemeral = true,
                )
            }
            override val embeds: List<MessageEmbed> = listOf()
            override val customEmojis: List<CustomEmoji> = manager.getCustomEmojis(content)
            override val stickers: List<Sticker> = listOf()
            override val referencedMessages: Flow<Message> = emptyFlow()

            override suspend fun getAuthor(): User = this@SlashCommandEvent.getAuthor()

            override suspend fun getMember(): Member? = this@SlashCommandEvent.getMember()

            override suspend fun getChannel(): MessageChannel = this@SlashCommandEvent.getChannel()

            override suspend fun getGuild(): Guild? = this@SlashCommandEvent.getGuild()
        }
}

suspend fun IReplyCallback.reply(
    message: MessageCreateData,
    channel: MessageChannel,
    deferReply: Boolean,
    ephemeralReply: Boolean,
    suppressEmbeds: Boolean,
): Message =
    if (deferReply) {
        val discordResponse = hook.sendMessage(message)
            .setSuppressEmbeds(suppressEmbeds)
            .await()
        DiscordMessage(discordResponse)
    } else {
        val interactionHook = reply(message)
            .setEphemeral(ephemeralReply)
            .setSuppressEmbeds(suppressEmbeds)
            .await()
        if (ephemeralReply) {
            FakeMessage(
                interactionHook.id,
                message.content,
                channel.manager.getSelf(),
                channel,
            )
        } else {
            val discordResponse = interactionHook.retrieveOriginal().await()
            DiscordMessage(discordResponse)
        }
    }
