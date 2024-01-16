package io.github.shaksternano.borgar.chat

import io.github.shaksternano.borgar.chat.command.Permission
import io.github.shaksternano.borgar.chat.entity.CustomEmoji
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.User

interface BotManager {

    val maxMessageContentLength: Int
    val maxFileSize: Long
    val maxFilesPerMessage: Int
    val emojiTypedPattern: Regex

    suspend fun getGuild(id: String): Guild?

    suspend fun getUser(id: String): User?

    fun getCustomEmojis(content: String): List<CustomEmoji>

    fun getEmojiName(typedEmoji: String): String

    fun emojiAsTyped(emoji: String): String

    fun getPermissionName(permission: Permission): String
}
