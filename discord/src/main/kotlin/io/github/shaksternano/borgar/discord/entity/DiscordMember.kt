package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Member
import io.github.shaksternano.borgar.chat.entity.Role
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.discord.DiscordManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.time.OffsetDateTime

data class DiscordMember(
    private val discordMember: net.dv8tion.jda.api.entities.Member
) : Member, DiscordPermissionHolder(discordMember) {

    override val id: String = discordMember.id
    override val manager: BotManager = DiscordManager[discordMember.jda]
    override val user: User = DiscordUser(discordMember.user)
    override val roles: Flow<Role> = discordMember.roles.map { DiscordRole(it) }.asFlow()
    override val timeoutEnd: OffsetDateTime? = discordMember.timeOutEnd
    private val guild: Guild = DiscordGuild(discordMember.guild)
    override val effectiveName: String = discordMember.effectiveName
    override val effectiveAvatarUrl: String = "${discordMember.effectiveAvatarUrl}?size=1024"
    override val asMention: String = discordMember.asMention
    override val asBasicMention: String = "@${discordMember.effectiveName}"

    override suspend fun getGuild(): Guild = guild
}
