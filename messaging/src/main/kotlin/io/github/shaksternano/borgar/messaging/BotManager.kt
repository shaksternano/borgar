package io.github.shaksternano.borgar.messaging

import io.github.shaksternano.borgar.messaging.command.Permission
import io.github.shaksternano.borgar.messaging.entity.*
import io.github.shaksternano.borgar.messaging.entity.channel.Channel
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

const val BOT_STATUS = "fortnite battle pass"

private val botManagersMutable: MutableList<BotManager> = mutableListOf()
val BOT_MANAGERS: List<BotManager> = botManagersMutable

interface BotManager {

    val platform: MessagingPlatform
    val selfId: String
    val ownerId: String
    val maxMessageContentLength: Int
    val maxFileSize: Long
    val maxFilesPerMessage: Int
    val emojiTypedRegex: Regex
    val typingDuration: Duration

    suspend fun getSelf(): User

    suspend fun getUser(id: String): User?

    suspend fun getChannel(id: String): Channel?

    suspend fun getGuild(id: String): Guild?

    suspend fun getGroup(id: String): Group?

    suspend fun getGuildCount(): Int

    fun getCustomEmojis(content: String): Flow<CustomEmoji>

    fun getMentionedUsers(content: String): Flow<User>

    fun getMentionedChannels(content: String): Flow<Channel>

    fun getMentionedRoles(content: String): Flow<Role>

    fun getEmojiName(typedEmoji: String): String

    fun emojiAsTyped(emoji: String): String

    fun getPermissionName(permission: Permission): String
}

fun registerBotManager(botManager: BotManager) {
    botManagersMutable += botManager
}
