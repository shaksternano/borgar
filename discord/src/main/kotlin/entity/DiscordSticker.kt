package com.shakster.borgar.discord.entity

import com.shakster.borgar.discord.DiscordManager
import com.shakster.borgar.messaging.BotManager
import com.shakster.borgar.messaging.entity.Sticker
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
