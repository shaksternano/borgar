package io.github.shaksternano.borgar.chat

import io.github.shaksternano.borgar.chat.entity.CustomEmoji
import io.github.shaksternano.borgar.chat.entity.Guild

interface BotManager {

    val maxMessageContentLength: Int
    val maxFileSize: Long
    val maxFilesPerMessage: Int
    val emojiTypedPattern: Regex

    suspend fun getGuild(id: String): Guild?

    fun getCustomEmojis(content: String): List<CustomEmoji>

    fun getEmojiName(typedEmoji: String): String

    fun emojiAsTyped(emoji: String): String
}
