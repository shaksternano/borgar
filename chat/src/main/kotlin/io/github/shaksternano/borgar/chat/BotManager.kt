package io.github.shaksternano.borgar.chat

import io.github.shaksternano.borgar.chat.command.Permission
import io.github.shaksternano.borgar.chat.entity.CustomEmoji
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Role
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import kotlin.time.Duration

interface BotManager {

    val maxMessageContentLength: Int
    val maxFileSize: Long
    val maxFilesPerMessage: Int
    val emojiTypedPattern: Regex
    val typingDuration: Duration
    val ownerId: String

    suspend fun getSelf(): User

    suspend fun getGuild(id: String): Guild?

    suspend fun getUser(id: String): User?

    fun getCustomEmojis(content: String): List<CustomEmoji>

    fun getMentionedUsers(content: String): List<User>

    fun getMentionedChannels(content: String): List<Channel>

    fun getMentionedRoles(content: String): List<Role>

    fun getEmojiName(typedEmoji: String): String

    fun emojiAsTyped(emoji: String): String

    fun getPermissionName(permission: Permission): String
}
