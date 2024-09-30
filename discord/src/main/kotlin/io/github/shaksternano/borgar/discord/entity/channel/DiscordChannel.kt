package io.github.shaksternano.borgar.discord.entity.channel

import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.entity.DiscordGroup
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.entity.BaseEntity
import io.github.shaksternano.borgar.messaging.entity.Group
import io.github.shaksternano.borgar.messaging.entity.Guild
import io.github.shaksternano.borgar.messaging.entity.channel.Channel
import net.dv8tion.jda.api.entities.channel.concrete.GroupChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.InteractionContextType

open class DiscordChannel protected constructor(
    val discordChannel: net.dv8tion.jda.api.entities.channel.Channel,
    context: InteractionContextType = InteractionContextType.UNKNOWN,
) : Channel, BaseEntity() {

    companion object {
        fun create(
            discordChannel: net.dv8tion.jda.api.entities.channel.Channel,
            context: InteractionContextType = InteractionContextType.UNKNOWN,
        ): DiscordChannel = when (discordChannel) {
            is MessageChannel -> DiscordMessageChannel(discordChannel, context)
            else -> DiscordChannel(discordChannel, context)
        }
    }

    override val manager: BotManager = DiscordManager[discordChannel.jda]
    override val id: String = discordChannel.id
    override val name: String = discordChannel.name
    override val environment: ChannelEnvironment = when (discordChannel) {
        is GuildChannel -> ChannelEnvironment.GUILD
        is GroupChannel -> ChannelEnvironment.GROUP
        else -> when (context) {
            InteractionContextType.BOT_DM -> ChannelEnvironment.DIRECT_MESSAGE
            else -> ChannelEnvironment.PRIVATE
        }
    }
    override val asMention: String = discordChannel.asMention
    override val asBasicMention: String = "#${discordChannel.name}"

    override suspend fun getGuild(): Guild? {
        return if (discordChannel is GuildChannel) {
            DiscordGuild(discordChannel.guild)
        } else {
            null
        }
    }

    override suspend fun getGroup(): Group? =
        if (discordChannel is GroupChannel) {
            DiscordGroup(discordChannel)
        } else {
            null
        }
}
