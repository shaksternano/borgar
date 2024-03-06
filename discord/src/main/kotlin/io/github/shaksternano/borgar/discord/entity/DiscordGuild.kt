package io.github.shaksternano.borgar.discord.entity

import io.github.shaksternano.borgar.chat.BotManager
import io.github.shaksternano.borgar.chat.command.Command
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.discord.await
import io.github.shaksternano.borgar.discord.command.toSlash

data class DiscordGuild(
    private val discordGuild: net.dv8tion.jda.api.entities.Guild
) : Guild, BaseEntity() {

    override val manager: BotManager = DiscordManager[discordGuild.jda]
    override val id: String = discordGuild.id
    override val name: String = discordGuild.name
    override val ownerId: String = discordGuild.ownerId
    override val iconUrl: String? = discordGuild.iconUrl?.let { "$it?size=1024" }
    override val bannerUrl: String? = discordGuild.bannerUrl?.let { "$it?size=4096" }
    override val splashUrl: String? = discordGuild.splashUrl?.let { "$it?size=4096" }
    override val maxFileSize: Long = discordGuild.maxFileSize
    override val publicRole: Role = DiscordRole(discordGuild.publicRole)

    override suspend fun getMember(userId: String): Member? =
        discordGuild.runCatching {
            DiscordMember(retrieveMemberById(userId).await())
        }.getOrNull()

    override suspend fun getCustomEmojis(): List<CustomEmoji> =
        discordGuild.retrieveEmojis()
            .await()
            .map {
                DiscordCustomEmoji(it, discordGuild.jda)
            }

    override suspend fun addCommand(command: Command) {
        discordGuild.upsertCommand(command.toSlash()).await()
    }

    override suspend fun deleteCommand(commandName: String) {
        val commands = discordGuild.retrieveCommands().await()
        val command = commands.find { it.name == commandName } ?: return
        discordGuild.deleteCommandById(command.id).await()
    }
}
