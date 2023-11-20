package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Member
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.discord.DiscordManager

data class DiscordMember(
    private val discordMember: net.dv8tion.jda.api.entities.Member
) : DiscordPermissionHolder(discordMember), Member {

    override val id: String = discordMember.id
    override val manager: BotManager = DiscordManager.get(discordMember.jda)
    override val user: User = DiscordUser(discordMember.user)
    override val guild: Guild = DiscordGuild(discordMember.guild)
    override val effectiveName: String = discordMember.effectiveName
    override val effectiveAvatarUrl: String = discordMember.effectiveAvatarUrl
    override val asMention: String = discordMember.asMention
}
