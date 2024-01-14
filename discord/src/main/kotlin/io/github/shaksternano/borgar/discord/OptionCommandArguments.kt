package io.github.shaksternano.borgar.discord

import io.github.shaksternano.borgar.chat.command.CommandArguments
import io.github.shaksternano.borgar.chat.entity.Attachment
import io.github.shaksternano.borgar.chat.entity.Mentionable
import io.github.shaksternano.borgar.chat.entity.Role
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.discord.entity.DiscordMentionable
import io.github.shaksternano.borgar.discord.entity.DiscordRole
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordChannel
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload

class OptionCommandArguments(
    private val interaction: CommandInteractionPayload,
    override val defaultKey: String?
) : CommandArguments {

    override fun hasKey(key: String): Boolean =
        interaction.getOption(key) != null

    override fun getString(key: String): String? =
        interaction.getOption(key)?.asString

    override fun getLong(key: String): Long? =
        interaction.getOption(key)
            ?.runCatching { asLong }
            ?.getOrNull()

    override fun getDouble(key: String): Double? =
        interaction.getOption(key)
            ?.runCatching { asDouble }
            ?.getOrNull()

    override fun getBoolean(key: String): Boolean? =
        interaction.getOption(key)
            ?.runCatching { asBoolean }
            ?.getOrNull()

    override suspend fun getUser(key: String): User? =
        interaction.getOption(key)
            ?.runCatching { asUser }
            ?.map { DiscordUser(it) }
            ?.getOrNull()

    override suspend fun getChannel(key: String): Channel? =
        interaction.getOption(key)
            ?.runCatching { asChannel }
            ?.map { DiscordChannel.create(it) }
            ?.getOrNull()

    override suspend fun getRole(key: String): Role? =
        interaction.getOption(key)
            ?.runCatching { asRole }
            ?.map { DiscordRole(it) }
            ?.getOrNull()

    override fun getMentionable(key: String): Mentionable? =
        interaction.getOption(key)
            ?.runCatching { asMentionable }
            ?.map { DiscordMentionable.create(it, interaction.jda) }
            ?.getOrNull()

    override fun getAttachment(key: String): Attachment? =
        interaction.getOption(key)
            ?.runCatching { asAttachment }
            ?.map {
                Attachment(
                    id = it.id,
                    url = it.url,
                    proxyUrl = it.proxyUrl,
                    fileName = it.fileName,
                    manager = DiscordManager.get(interaction.jda)
                )
            }
            ?.getOrNull()
}
