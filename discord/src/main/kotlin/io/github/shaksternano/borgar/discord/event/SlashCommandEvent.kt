package io.github.shaksternano.borgar.discord.event

import dev.minn.jda.ktx.messages.MessageCreateBuilder
import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.command.*
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.await
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
import io.github.shaksternano.borgar.discord.toFileUpload
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.time.OffsetDateTime

class SlashCommandEvent(
    private val event: SlashCommandInteractionEvent
) : CommandEvent {

    override val id: String = event.id
    override val manager: BotManager = DiscordManager.get(event.jda)
    override val timeCreated: OffsetDateTime = event.timeCreated

    private val user: User = DiscordUser(event.user)
    private val channel: MessageChannel = DiscordMessageChannel(event.channel)
    private val guild: Guild? = event.guild?.let { DiscordGuild(it) }

    private var replied = false

    override suspend fun getAuthor(): User = user

    override suspend fun getChannel(): MessageChannel = channel

    override suspend fun getGuild(): Guild? = guild

    override suspend fun getReferencedMessage(): Message? = null

    override suspend fun reply(response: CommandResponse): Message {
        val replyBuilder = MessageCreateBuilder(
            content = response.content,
            files = response.files.map(DataSource::toFileUpload),
        ).build()
        val discordResponseMessage = if (replied) {
            event.channel.sendMessage(replyBuilder)
                .setSuppressEmbeds(response.suppressEmbeds)
                .await()
        } else {
            replied = true
            if (response.deferReply) {
                event.hook.sendMessage(replyBuilder)
                    .setSuppressEmbeds(response.suppressEmbeds)
                    .await()
            } else {
                val interactionHook = event.reply(replyBuilder)
                    .setEphemeral(response.ephemeral)
                    .setSuppressEmbeds(response.suppressEmbeds)
                    .await()
                if (response.ephemeral) {
                    return FakeMessage(
                        interactionHook.id,
                        manager,
                        response.content,
                        manager.getSelf(),
                        channel,
                    )
                }
                interactionHook.retrieveOriginal().await()
            }
        }
        return DiscordMessage(discordResponseMessage)
    }

    override fun asMessageIntersection(arguments: CommandArguments): CommandMessageIntersection =
        object : CommandMessageIntersection {
            override val id: String = this@SlashCommandEvent.id
            override val manager: BotManager = this@SlashCommandEvent.manager
            override val content: String = arguments.getDefaultStringOrEmpty()
            override val attachments: List<Attachment> = listOfNotNull(arguments.getDefaultAttachment())
            override val embeds: List<MessageEmbed> = listOf()
            override val customEmojis: List<CustomEmoji> = manager.getCustomEmojis(content)

            override suspend fun getAuthor(): User = this@SlashCommandEvent.getAuthor()

            override suspend fun getChannel(): MessageChannel = this@SlashCommandEvent.getChannel()

            override suspend fun getGuild(): Guild? = this@SlashCommandEvent.getGuild()

            override suspend fun getReferencedMessage(): Message? = null
        }
}
