package io.github.shaksternano.borgar.discord

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.entity.CustomEmoji
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.discord.entity.DiscordCustomEmoji
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.MessageMentionsImpl

class DiscordManager(
    private val jda: JDA,
) : BotManager {
    companion object {
        private val managers: MutableMap<JDA, BotManager> = mutableMapOf()

        fun create(jda: JDA) {
            managers[jda] = DiscordManager(jda)
        }

        fun get(jda: JDA): BotManager {
            return managers.getOrElse(jda) {
                throw IllegalArgumentException("No manager for $jda")
            }
        }
    }

    override val maxMessageContentLength: Int = Message.MAX_CONTENT_LENGTH
    override val maxFileSize: Long = Message.MAX_FILE_SIZE.toLong()
    override val maxFilesPerMessage: Int = Message.MAX_FILE_AMOUNT
    override val emojiTypedPattern: Regex = ":[A-Za-z0-9]+:".toRegex()

    override suspend fun getGuild(id: String): Guild? {
        return jda.getGuildById(id)?.let { DiscordGuild(it) }
    }

    override fun getCustomEmojis(content: String): List<CustomEmoji> {
        if (content.isBlank()) return emptyList()
        val mentions = MessageMentionsImpl(
            jda as JDAImpl,
            null,
            content,
            false,
            DataArray.empty(),
            DataArray.empty(),
        )
        return mentions.customEmojis.map { DiscordCustomEmoji(it, jda) }
    }

    override fun getEmojiName(typedEmoji: String): String = typedEmoji.removeSurrounding(":")

    override fun emojiAsTyped(emoji: String): String = ":$emoji:"
}
