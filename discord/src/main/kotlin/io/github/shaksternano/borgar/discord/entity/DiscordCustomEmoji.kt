package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.entity.BaseEntity
import io.github.shaksternano.borgar.messaging.entity.CustomEmoji

class DiscordCustomEmoji(
    discordEmoji: net.dv8tion.jda.api.entities.emoji.CustomEmoji,
    override val manager: BotManager,
) : CustomEmoji, BaseEntity() {

    override val id: String = discordEmoji.id
    override val name: String = discordEmoji.name
    override val imageUrl: String = discordEmoji.imageUrl
    override val asMention: String = discordEmoji.asMention
    override val asBasicMention: String = ":${name}:"

    override fun toString(): String {
        return "DiscordCustomEmoji(" +
            "name='$name'" +
            ", id='$id'" +
            ", imageUrl='$imageUrl'" +
            ")"
    }
}
