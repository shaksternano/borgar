package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.entity.BaseEntity
import io.github.shaksternano.borgar.messaging.entity.CustomEmoji
import net.dv8tion.jda.api.JDA

data class DiscordCustomEmoji(
    private val discordEmoji: net.dv8tion.jda.api.entities.emoji.CustomEmoji,
    private val jda: JDA,
) : CustomEmoji, BaseEntity() {

    override val name: String = discordEmoji.name
    override val imageUrl: String = discordEmoji.imageUrl
    override val asMention: String = discordEmoji.asMention
    override val id: String = discordEmoji.id
    override val manager: BotManager = DiscordManager[jda]
    override val asBasicMention: String = ":${name}:"
}
