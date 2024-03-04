package io.github.shaksternano.io.github.shaksternano.borgar.revolt

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.command.Permission
import io.github.shaksternano.borgar.chat.entity.CustomEmoji
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Role
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.io.github.shaksternano.borgar.revolt.websocket.RevoltWebSocketClient
import kotlin.time.Duration

class RevoltManager(
    private val token: String,
) : BotManager {

    override val maxMessageContentLength: Int
        get() = TODO("Not yet implemented")
    override val maxFileSize: Long
        get() = TODO("Not yet implemented")
    override val maxFilesPerMessage: Int
        get() = TODO("Not yet implemented")
    override val emojiTypedPattern: Regex
        get() = TODO("Not yet implemented")
    override val typingDuration: Duration
        get() = TODO("Not yet implemented")
    override val ownerId: String
        get() = TODO("Not yet implemented")

    suspend fun awaitReady() {
        RevoltWebSocketClient(token).awaitReady()
    }

    override suspend fun getSelf(): User {
        TODO("Not yet implemented")
    }

    override suspend fun getGuild(id: String): Guild? {
        TODO("Not yet implemented")
    }

    override suspend fun getUser(id: String): User? {
        TODO("Not yet implemented")
    }

    override suspend fun getGuildCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getCustomEmojis(content: String): List<CustomEmoji> {
        TODO("Not yet implemented")
    }

    override fun getMentionedUsers(content: String): List<User> {
        TODO("Not yet implemented")
    }

    override fun getMentionedChannels(content: String): List<Channel> {
        TODO("Not yet implemented")
    }

    override fun getMentionedRoles(content: String): List<Role> {
        TODO("Not yet implemented")
    }

    override fun getEmojiName(typedEmoji: String): String {
        TODO("Not yet implemented")
    }

    override fun emojiAsTyped(emoji: String): String {
        TODO("Not yet implemented")
    }

    override fun getPermissionName(permission: Permission): String {
        TODO("Not yet implemented")
    }
}
