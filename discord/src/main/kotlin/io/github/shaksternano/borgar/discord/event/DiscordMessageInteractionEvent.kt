package io.github.shaksternano.borgar.discord.event

import dev.minn.jda.ktx.messages.MessageCreateBuilder
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.await
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import io.github.shaksternano.borgar.discord.entity.DiscordMember
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
import io.github.shaksternano.borgar.discord.toFileUpload
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.entity.Guild
import io.github.shaksternano.borgar.messaging.entity.Member
import io.github.shaksternano.borgar.messaging.entity.Message
import io.github.shaksternano.borgar.messaging.entity.User
import io.github.shaksternano.borgar.messaging.entity.channel.MessageChannel
import io.github.shaksternano.borgar.messaging.event.MessageInteractionEvent
import io.github.shaksternano.borgar.messaging.interaction.InteractionResponse
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData

class DiscordMessageInteractionEvent(
    private val discordEvent: MessageContextInteractionEvent,
) : MessageInteractionEvent {

    override val manager: BotManager = DiscordManager[discordEvent.jda]
    override val id: String = discordEvent.id
    override val name: String = discordEvent.name
    override val message: Message = DiscordMessage(discordEvent.target)

    private val user: User = DiscordUser(discordEvent.user)
    private val member: Member? = discordEvent.member?.let { DiscordMember(it) }
    private val channel: MessageChannel = DiscordMessageChannel(discordEvent.target.channel)
    private val guild: Guild? = discordEvent.guild?.let { DiscordGuild(it) }

    override var ephemeralReply: Boolean = false
    private var deferReply: Boolean = false
    private var replied: Boolean = false

    override suspend fun getAuthor(): User = user

    override suspend fun getAuthorMember(): Member? = member

    override suspend fun getChannel(): MessageChannel = channel

    override suspend fun getGuild(): Guild? = guild

    override suspend fun deferReply() {
        if (deferReply) return
        discordEvent.deferReply(ephemeralReply).await()
        deferReply = true
    }

    override suspend fun reply(response: InteractionResponse): Message {
        if (replied) throw IllegalStateException("Already replied to this interaction")
        return discordEvent.reply(
            response.convert(),
            deferReply,
            ephemeralReply,
            response.suppressEmbeds,
        ).also {
            replied = true
        }
    }
}

fun InteractionResponse.convert(): MessageCreateData =
    MessageCreateBuilder(
        content = content,
        files = files.map { it.toFileUpload() }
    ).build()
