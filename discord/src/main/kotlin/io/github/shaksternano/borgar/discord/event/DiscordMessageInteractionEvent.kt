package io.github.shaksternano.borgar.discord.event

import dev.minn.jda.ktx.messages.MessageCreateBuilder
import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Member
import io.github.shaksternano.borgar.chat.entity.Message
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.MessageChannel
import io.github.shaksternano.borgar.chat.event.MessageInteractionEvent
import io.github.shaksternano.borgar.chat.interaction.MessageInteractionResponse
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.await
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import io.github.shaksternano.borgar.discord.entity.DiscordMember
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordMessageChannel
import io.github.shaksternano.borgar.discord.toFileUpload
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData

class DiscordMessageInteractionEvent(
    private val event: MessageContextInteractionEvent,
) : MessageInteractionEvent {

    override val manager: BotManager = DiscordManager[event.jda]
    override val id: String = event.id
    override val name: String = event.name
    override val message: Message = DiscordMessage(event.target)

    private val user: User = DiscordUser(event.user)
    private val member: Member? = event.member?.let { DiscordMember(it) }
    private val channel: MessageChannel = DiscordMessageChannel(event.channel!!)
    private val guild: Guild? = event.guild?.let { DiscordGuild(it) }

    override var ephemeralReply: Boolean = false

    override suspend fun getUser(): User = user

    override suspend fun getMember(): Member? = member

    override suspend fun getChannel(): MessageChannel = channel

    override suspend fun getGuild(): Guild? = guild

    private var deferReply: Boolean = false

    override suspend fun deferReply() {
        event.deferReply(ephemeralReply).await()
        deferReply = true
    }

    override suspend fun reply(response: MessageInteractionResponse): Message =
        event.reply(
            response.convert(),
            channel,
            deferReply,
            ephemeralReply,
            response.suppressEmbeds,
        )

    private fun MessageInteractionResponse.convert(): MessageCreateData {
        val builder = MessageCreateBuilder(
            content = content,
            files = files.map { it.toFileUpload() }
        )
        return builder.build()
    }
}
