package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.entity.channel.DiscordChannel
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.entity.Mentionable
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.interactions.InteractionContextType

class DiscordMentionable(
    mentionable: net.dv8tion.jda.api.entities.IMentionable,
    jda: JDA,
) : Mentionable {

    companion object {
        fun create(
            mentionable: net.dv8tion.jda.api.entities.IMentionable,
            jda: JDA,
            context: InteractionContextType = InteractionContextType.UNKNOWN,
        ): Mentionable = when (mentionable) {
            is User -> DiscordUser(mentionable)
            is Member -> DiscordMember(mentionable)
            is Channel -> DiscordChannel.create(mentionable, context)
            is Role -> DiscordRole(mentionable)
            is CustomEmoji -> DiscordCustomEmoji(mentionable, DiscordManager[jda])
            else -> DiscordMentionable(mentionable, jda)
        }
    }

    override val id: String = mentionable.id
    override val manager: BotManager = DiscordManager[jda]
    override val asMention: String = mentionable.asMention
    override val asBasicMention: String = asMention
}
