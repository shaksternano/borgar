package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.entity.Sticker
import net.dv8tion.jda.api.JDA

data class DiscordSticker(
    private val discordSticker: net.dv8tion.jda.api.entities.sticker.Sticker,
    private val jda: JDA,
) : Sticker {

    override val id: String = discordSticker.id
    override val manager: BotManager = DiscordManager[jda]
    override val name: String = discordSticker.name
    override val imageUrl: String = discordSticker.iconUrl
}
