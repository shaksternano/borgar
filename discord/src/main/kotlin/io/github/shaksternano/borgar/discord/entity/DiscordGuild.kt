package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.await

data class DiscordGuild(
    private val discordGuild: net.dv8tion.jda.api.entities.Guild
) : BaseEntity(), Guild {

    override val id: String = discordGuild.id
    override val manager: BotManager = DiscordManager[discordGuild.jda]
    override val name: String = discordGuild.name
    override val iconUrl: String? = discordGuild.iconUrl?.let { "$it?size=1024" }
    override val bannerUrl: String? = discordGuild.bannerUrl?.let { "$it?size=4096" }
    override val splashUrl: String? = discordGuild.splashUrl?.let { "$it?size=4096" }

    override suspend fun getMember(userId: String): Member? =
        discordGuild.runCatching {
            DiscordMember(retrieveMemberById(userId).await())
        }.getOrNull()

    override suspend fun getCustomEmojis(): List<CustomEmoji> =
        discordGuild.retrieveEmojis()
            .await()
            .map {
                DiscordCustomEmoji(it, discordGuild.jda)
            }

    override suspend fun getMaxFileSize(): Long =
        discordGuild.boostTier.maxFileSize

    override suspend fun getPublicRole(): Role =
        DiscordRole(discordGuild.publicRole)
}
