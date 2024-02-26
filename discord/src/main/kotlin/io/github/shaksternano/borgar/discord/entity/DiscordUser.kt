package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.await

data class DiscordUser(
    private val discordUser: net.dv8tion.jda.api.entities.User
) : BaseEntity(), User {

    override val id: String = discordUser.id
    override val manager: BotManager = DiscordManager[discordUser.jda]
    override val effectiveName: String = discordUser.effectiveName
    override val effectiveAvatarUrl: String = discordUser.effectiveAvatarUrl
    override val isSelf: Boolean = discordUser.jda.selfUser == discordUser
    override val asMention: String = discordUser.asMention
    override val asBasicMention: String = "@${discordUser.effectiveName}"

    override suspend fun getBannerUrl(): String? =
        discordUser.retrieveProfile()
            .useCache(false)
            .await()
            .bannerUrl
}
