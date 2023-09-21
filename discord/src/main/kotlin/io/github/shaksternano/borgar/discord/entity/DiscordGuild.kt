package io.github.shaksternano.borgar.discord.entity

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Member
import io.github.shaksternano.borgar.discord.DiscordManager

data class DiscordGuild(
    private val discordGuild: net.dv8tion.jda.api.entities.Guild
) : BaseEntity(), Guild {

    override val id: String = discordGuild.id
    override val manager: BotManager = DiscordManager.get(discordGuild.jda)

    override suspend fun getMember(userId: String): Member? {
        return discordGuild.runCatching {
            DiscordMember(retrieveMemberById(userId).await())
        }.getOrNull()
    }

    override suspend fun getMaxFileSize(): Long =
        discordGuild.boostTier.maxFileSize
}
