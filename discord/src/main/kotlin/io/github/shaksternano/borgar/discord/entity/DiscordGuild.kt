package io.github.shaksternano.borgar.discord.entity

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.discord.DiscordManager

data class DiscordGuild(
    private val discordGuild: net.dv8tion.jda.api.entities.Guild
) : BaseEntity(), Guild {

    override val id: String = discordGuild.id
    override val manager: BotManager = DiscordManager.get(discordGuild.jda)
    override val name: String = discordGuild.name

    override suspend fun getMember(userId: String): Member? {
        return discordGuild.runCatching {
            DiscordMember(retrieveMemberById(userId).await())
        }.getOrNull()
    }

    override fun getCustomEmojis(): List<CustomEmoji> =
        discordGuild.emojiCache.map { DiscordCustomEmoji(it, discordGuild.jda) }

    override suspend fun getMaxFileSize(): Long =
        discordGuild.boostTier.maxFileSize

    override suspend fun getPublicRole(): Role =
        DiscordRole(discordGuild.publicRole)
}
