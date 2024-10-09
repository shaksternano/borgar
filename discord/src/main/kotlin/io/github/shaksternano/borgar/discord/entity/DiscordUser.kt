package io.github.shaksternano.borgar.discord.entity

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.entity.BaseEntity
import io.github.shaksternano.borgar.messaging.entity.User

data class DiscordUser(
    private val discordUser: net.dv8tion.jda.api.entities.User,
) : User, BaseEntity() {

    override val id: String = discordUser.id
    override val manager: BotManager = DiscordManager[discordUser.jda]
    override val name: String = discordUser.name
    override val effectiveName: String = discordUser.effectiveName
    override val effectiveAvatarUrl: String = "${discordUser.effectiveAvatarUrl}?size=1024"
    override val isSelf: Boolean = discordUser.jda.selfUser == discordUser
    override val isBot: Boolean = discordUser.isBot
    override val asMention: String = discordUser.asMention
    override val asBasicMention: String = "@${discordUser.effectiveName}"

    override suspend fun getBannerUrl(): String? =
        discordUser.getBannerUrl()
}

suspend fun net.dv8tion.jda.api.entities.User.getBannerUrl(): String? =
    retrieveProfile()
        .useCache(false)
        .await()
        .bannerUrl
        ?.let {
            "$it?size=1024"
        }
