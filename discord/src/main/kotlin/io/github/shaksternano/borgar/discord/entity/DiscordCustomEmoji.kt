package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.CustomEmoji
import io.github.shaksternano.borgar.discord.DiscordManager
import net.dv8tion.jda.api.JDA

data class DiscordCustomEmoji(
    private val discordEmoji: net.dv8tion.jda.api.entities.emoji.CustomEmoji,
    private val jda: JDA,
) : CustomEmoji, BaseEntity() {

    override val imageUrl: String = discordEmoji.imageUrl
    override val asMention: String = discordEmoji.asMention
    override val id: String = discordEmoji.id
    override val manager: BotManager = DiscordManager.get(jda)
}
