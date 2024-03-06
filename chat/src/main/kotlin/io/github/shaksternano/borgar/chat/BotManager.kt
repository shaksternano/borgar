package io.github.shaksternano.borgar.chat

import io.github.shaksternano.borgar.chat.command.Permission
import io.github.shaksternano.borgar.chat.entity.CustomEmoji
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Role
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

const val BOT_STATUS = "fortnite battle pass"

interface BotManager {

    val platform: String
    val selfId: String
    val ownerId: String
    val maxMessageContentLength: Int
    val maxFileSize: Long
    val maxFilesPerMessage: Int
    val emojiTypedRegex: Regex
    val typingDuration: Duration

    suspend fun getSelf(): User

    suspend fun getGuild(id: String): Guild?

    suspend fun getUser(id: String): User?

    suspend fun getGuildCount(): Int

    fun getCustomEmojis(content: String): Flow<CustomEmoji>

    fun getMentionedUsers(content: String): Flow<User>

    fun getMentionedChannels(content: String): Flow<Channel>

    fun getMentionedRoles(content: String): Flow<Role>

    fun getEmojiName(typedEmoji: String): String

    fun emojiAsTyped(emoji: String): String

    fun getPermissionName(permission: Permission): String
}
