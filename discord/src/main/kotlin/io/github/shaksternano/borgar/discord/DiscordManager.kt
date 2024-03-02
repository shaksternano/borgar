package io.github.shaksternano.borgar.discord

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.command.Permission
import io.github.shaksternano.borgar.chat.entity.CustomEmoji
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Role
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.discord.entity.DiscordCustomEmoji
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import io.github.shaksternano.borgar.discord.entity.DiscordRole
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordChannel
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Mentions
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.MessageMentionsImpl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DiscordManager(
    private val jda: JDA,
    override val ownerId: String
) : BotManager {

    companion object {
        private val managers: MutableMap<JDA, BotManager> = mutableMapOf()

        suspend fun create(jda: JDA) {
            val ownerId = jda.retrieveApplicationInfo().await().owner.id
            managers[jda] = DiscordManager(jda, ownerId)
        }

        operator fun get(jda: JDA): BotManager =
            managers.getOrElse(jda) {
                throw IllegalArgumentException("No manager for $jda")
            }
    }

    override val maxMessageContentLength: Int = Message.MAX_CONTENT_LENGTH
    override val maxFileSize: Long = Message.MAX_FILE_SIZE.toLong()
    override val maxFilesPerMessage: Int = Message.MAX_FILE_AMOUNT
    override val emojiTypedPattern: Regex = ":[A-Za-z0-9]+:".toRegex()
    override val typingDuration: Duration = 5.seconds

    override suspend fun getSelf(): User =
        DiscordUser(jda.selfUser)

    override suspend fun getGuild(id: String): Guild? =
        jda.getGuildById(id)?.let { DiscordGuild(it) }

    override suspend fun getUser(id: String): User? = runCatching {
        jda.retrieveUserById(id).await()?.let { DiscordUser(it) }
    }.getOrNull()

    override suspend fun getGuildCount(): Int =
        jda.guildCache.size().toInt()

    private fun getMentions(content: String): Mentions = MessageMentionsImpl(
        jda as JDAImpl,
        null,
        content,
        false,
        DataArray.empty(),
        DataArray.empty(),
    )

    override fun getCustomEmojis(content: String): List<CustomEmoji> =
        if (content.isBlank()) emptyList()
        else getMentions(content).customEmojis.map { DiscordCustomEmoji(it, jda) }

    override fun getMentionedUsers(content: String): List<User> =
        if (content.isBlank()) emptyList()
        else getMentions(content).users.map { DiscordUser(it) }

    override fun getMentionedChannels(content: String): List<Channel> =
        if (content.isBlank()) emptyList()
        else getMentions(content).channels.map { DiscordChannel.create(it) }

    override fun getMentionedRoles(content: String): List<Role> =
        if (content.isBlank()) emptyList()
        else getMentions(content).roles.map { DiscordRole(it) }

    override fun getEmojiName(typedEmoji: String): String = typedEmoji.removeSurrounding(":")

    override fun emojiAsTyped(emoji: String): String = ":$emoji:"

    override fun getPermissionName(permission: Permission): String =
        permission.toDiscord().getName()
}
