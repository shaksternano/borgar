package io.github.shaksternano.borgar.discord.entity.channel

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

open class DiscordChannel(
    private val discordChannel: net.dv8tion.jda.api.entities.channel.Channel
) : BaseEntity(), Channel {

    companion object {
        fun create(jdaChannel: net.dv8tion.jda.api.entities.channel.Channel): DiscordChannel = when (jdaChannel) {
            is MessageChannel -> DiscordMessageChannel(jdaChannel)
            else -> DiscordChannel(jdaChannel)
        }
    }

    override val id: String = discordChannel.id
    override val manager: BotManager = DiscordManager.get(discordChannel.jda)
    override val asMention: String = discordChannel.asMention
    override val name: String = discordChannel.name

    override suspend fun getGuild(): Guild? {
        return if (discordChannel is GuildChannel) {
            DiscordGuild(discordChannel.guild)
        } else {
            null
        }
    }
}
