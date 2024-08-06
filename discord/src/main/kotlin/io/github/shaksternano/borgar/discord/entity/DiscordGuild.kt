package io.github.shaksternano.borgar.discord.entity

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.command.toSlash
import io.github.shaksternano.borgar.discord.ifDetachedOrElse
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.command.Command
import io.github.shaksternano.borgar.messaging.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import net.dv8tion.jda.api.entities.Message

data class DiscordGuild(
    private val discordGuild: net.dv8tion.jda.api.entities.Guild,
) : Guild, BaseEntity() {

    override val manager: BotManager = DiscordManager[discordGuild.jda]
    override val id: String = discordGuild.id
    override val name: String? = discordGuild.ifDetachedOrElse(null) {
        discordGuild.name
    }
    override val ownerId: String? = discordGuild.ifDetachedOrElse(null) {
        discordGuild.ownerId
    }
    override val iconUrl: String? = discordGuild.ifDetachedOrElse(null) {
        discordGuild.iconUrl?.let { "$it?size=1024" }
    }
    override val bannerUrl: String? = discordGuild.ifDetachedOrElse(null) {
        discordGuild.bannerUrl?.let { "$it?size=4096" }
    }
    override val splashUrl: String? = discordGuild.ifDetachedOrElse(null) {
        discordGuild.splashUrl?.let { "$it?size=4096" }
    }
    override val maxFileSize: Long = discordGuild.ifDetachedOrElse(Message.MAX_FILE_SIZE.toLong()) {
        discordGuild.maxFileSize
    }
    override val publicRole: Role? = discordGuild.ifDetachedOrElse(null) {
        DiscordRole(discordGuild.publicRole)
    }
    override val customEmojis: Flow<CustomEmoji> = discordGuild.ifDetachedOrElse(emptyFlow()) {
        flow {
            discordGuild.retrieveEmojis()
                .await()
                .map {
                    DiscordCustomEmoji(it, discordGuild.jda)
                }
                .forEach {
                    emit(it)
                }
        }
    }

    override suspend fun getMember(userId: String): Member? =
        discordGuild.runCatching {
            DiscordMember(retrieveMemberById(userId).await())
        }.getOrNull()

    override suspend fun addCommand(command: Command) {
        discordGuild.upsertCommand(command.toSlash()).await()
    }

    override suspend fun deleteCommand(commandName: String) {
        val commands = discordGuild.retrieveCommands().await()
        val command = commands.find { it.name == commandName } ?: return
        discordGuild.deleteCommandById(command.id).await()
    }
}
