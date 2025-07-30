package com.shakster.borgar.discord.entity

import com.shakster.borgar.discord.DiscordManager
import com.shakster.borgar.messaging.BotManager
import com.shakster.borgar.messaging.entity.Role

data class DiscordRole(
    private val discordRole: net.dv8tion.jda.api.entities.Role,
) : Role, DiscordPermissionHolder(discordRole) {

    override val id: String = discordRole.id
    override val manager: BotManager = DiscordManager[discordRole.jda]
    override val name: String = discordRole.name
    override val asMention: String = discordRole.asMention
    override val asBasicMention: String =
        if (discordRole.isPublicRole) discordRole.name
        else "@${discordRole.name}"
}
