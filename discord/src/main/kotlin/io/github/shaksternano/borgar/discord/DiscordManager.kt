package io.github.shaksternano.borgar.discord

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.generics.getChannel
import io.github.shaksternano.borgar.discord.entity.DiscordCustomEmoji
import io.github.shaksternano.borgar.discord.entity.DiscordGuild
import io.github.shaksternano.borgar.discord.entity.DiscordRole
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordChannel
import io.github.shaksternano.borgar.discord.util.toDiscord
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.MessagingPlatform
import io.github.shaksternano.borgar.messaging.command.Permission
import io.github.shaksternano.borgar.messaging.entity.*
import io.github.shaksternano.borgar.messaging.entity.channel.Channel
import io.github.shaksternano.borgar.messaging.registerBotManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Mentions
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.MessageMentionsImpl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DiscordManager(
    private val jda: JDA,
    override val ownerId: String,
) : BotManager {

    companion object {
        private val managers: MutableMap<JDA, BotManager> = mutableMapOf()

        suspend fun create(jda: JDA) {
            val ownerId = jda.retrieveApplicationInfo().await().owner.id
            val manager = DiscordManager(jda, ownerId)
            managers[jda] = manager
            registerBotManager(manager)
        }

        operator fun get(jda: JDA): BotManager =
            managers.getOrElse(jda) {
                throw IllegalArgumentException("No manager for $jda")
            }
    }

    override val platform: MessagingPlatform = MessagingPlatform.DISCORD
    override val selfId: String = jda.selfUser.id
    override val maxMessageContentLength: Int = Message.MAX_CONTENT_LENGTH
    override val maxFileSize: Long = 10 shl 20
    override val maxFilesPerMessage: Int = Message.MAX_FILE_AMOUNT
    override val emojiTypedRegex: Regex = ":[A-Za-z0-9]+:".toRegex()
    override val typingDuration: Duration = 5.seconds
    override val commandAutoCompleteMaxSuggestions: Int = OptionData.MAX_CHOICES

    override suspend fun getSelf(): User =
        DiscordUser(jda.selfUser)

    override suspend fun getUser(id: String): User? = runCatching {
        jda.retrieveUserById(id).await()?.let { DiscordUser(it) }
    }.getOrNull()

    override suspend fun getChannel(id: String): Channel? =
        jda.getChannel(id)?.let { DiscordChannel.create(it) }

    override suspend fun getGuild(id: String): Guild? =
        jda.getGuildById(id)?.let { DiscordGuild(it) }

    override suspend fun getGroup(id: String): Group? =
        getChannel(id)?.getGroup()

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

    override fun getCustomEmojis(content: String): Flow<CustomEmoji> =
        if (content.isBlank()) emptyFlow()
        else getMentions(content).customEmojis.map { DiscordCustomEmoji(it, jda) }.asFlow()

    override fun getMentionedUsers(content: String): Flow<User> =
        if (content.isBlank()) emptyFlow()
        else getMentions(content).users.map { DiscordUser(it) }.asFlow()

    override fun getMentionedChannels(content: String): Flow<Channel> =
        if (content.isBlank()) emptyFlow()
        else getMentions(content).channels.map { DiscordChannel.create(it) }.asFlow()

    override fun getMentionedRoles(content: String): Flow<Role> =
        if (content.isBlank()) emptyFlow()
        else getMentions(content).roles.map { DiscordRole(it) }.asFlow()

    override fun getEmojiName(typedEmoji: String): String = typedEmoji.removeSurrounding(":")

    override fun emojiAsTyped(emoji: String): String = ":$emoji:"

    override fun getPermissionName(permission: Permission): String =
        permission.toDiscord().name
}
