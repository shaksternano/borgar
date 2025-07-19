package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.entity.Role

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
