package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.ifDetachedOrElse
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.entity.Guild
import io.github.shaksternano.borgar.messaging.entity.Member
import io.github.shaksternano.borgar.messaging.entity.Role
import io.github.shaksternano.borgar.messaging.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import java.time.OffsetDateTime

data class DiscordMember(
    private val discordMember: net.dv8tion.jda.api.entities.Member,
) : Member, DiscordPermissionHolder(discordMember) {

    override val id: String = discordMember.id
    override val manager: BotManager = DiscordManager[discordMember.jda]
    override val user: User = DiscordUser(discordMember.user)
    override val roles: Flow<Role> = discordMember.ifDetachedOrElse(emptyFlow()) {
        discordMember.roles.map { DiscordRole(it) }.asFlow()
    }
    override val timeoutEnd: OffsetDateTime? = discordMember.timeOutEnd
    private val guild: Guild = DiscordGuild(discordMember.guild)
    override val effectiveName: String = discordMember.effectiveName
    override val effectiveAvatarUrl: String = "${discordMember.effectiveAvatarUrl}?size=1024"
    override val asMention: String = discordMember.asMention
    override val asBasicMention: String = "@${discordMember.effectiveName}"

    override suspend fun getGuild(): Guild = guild
}
