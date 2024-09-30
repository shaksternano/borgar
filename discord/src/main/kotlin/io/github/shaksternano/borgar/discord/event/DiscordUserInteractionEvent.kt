package io.github.shaksternano.borgar.discord.event

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import io.github.shaksternano.borgar.discord.entity.DiscordMember
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordChannel
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.entity.Guild
import io.github.shaksternano.borgar.messaging.entity.Member
import io.github.shaksternano.borgar.messaging.entity.Message
import io.github.shaksternano.borgar.messaging.entity.User
import io.github.shaksternano.borgar.messaging.entity.channel.Channel
import io.github.shaksternano.borgar.messaging.event.UserInteractionEvent
import io.github.shaksternano.borgar.messaging.interaction.InteractionResponse
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

class DiscordUserInteractionEvent(
    private val discordEvent: UserContextInteractionEvent,
) : UserInteractionEvent {

    override val manager: BotManager = DiscordManager[discordEvent.jda]
    override val id: String = discordEvent.id
    override val name: String = discordEvent.name
    override var ephemeralReply: Boolean = false

    override val user: User = DiscordUser(discordEvent.target)
    private val member: Member? = discordEvent.member?.let { DiscordMember(it) }
    private val channel: Channel? = discordEvent.channel?.let { DiscordChannel.create(it, discordEvent.context) }
    private val guild: Guild? = discordEvent.guild?.let { DiscordGuild(it) }

    private var deferReply: Boolean = false
    private var replied: Boolean = false

    override suspend fun getAuthor(): User = user

    override suspend fun getAuthorMember(): Member? = member

    override suspend fun getChannel(): Channel? = channel

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
