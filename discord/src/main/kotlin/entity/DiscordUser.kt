package com.shakster.borgar.discord.entity

import com.shakster.borgar.discord.DiscordManager
import com.shakster.borgar.messaging.BotManager
import com.shakster.borgar.messaging.entity.BaseEntity
import com.shakster.borgar.messaging.entity.User
import dev.minn.jda.ktx.coroutines.await

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
