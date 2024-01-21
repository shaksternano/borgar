package io.github.shaksternano.borgar.discord

import io.github.shaksternano.borgar.chat.command.CommandArgumentType
import io.github.shaksternano.borgar.chat.command.CommandArguments
import io.github.shaksternano.borgar.chat.command.SimpleCommandArgumentType
import io.github.shaksternano.borgar.chat.entity.Attachment
import io.github.shaksternano.borgar.core.util.hash
import io.github.shaksternano.borgar.core.util.kClass
import io.github.shaksternano.borgar.discord.entity.DiscordMentionable
import io.github.shaksternano.borgar.discord.entity.DiscordRole
import io.github.shaksternano.borgar.discord.entity.DiscordUser
import io.github.shaksternano.borgar.discord.entity.channel.DiscordChannel
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload

class OptionCommandArguments(
    private val interaction: CommandInteractionPayload,
    override val defaultKey: String?
) : CommandArguments {

    override fun contains(key: String): Boolean =
        interaction.getOption(key) != null

    override fun <T> get(key: String, argumentType: SimpleCommandArgumentType<T>): T? =
        getValue(key, argumentType)

    override suspend fun <T> getSuspend(key: String, argumentType: CommandArgumentType<T>): T? =
        getValue(key, argumentType)

    private fun <T> getValue(key: String, argumentType: CommandArgumentType<T>): T? {
        val optionMapping = interaction.getOption(key) ?: return null
        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        return when (argumentType) {
            CommandArgumentType.STRING -> optionMapping.asString
            CommandArgumentType.INTEGER -> runCatching { optionMapping.asLong.toInt() }
                .getOrNull()
            CommandArgumentType.LONG -> runCatching { optionMapping.asLong }
                .getOrNull()

            CommandArgumentType.DOUBLE -> runCatching { optionMapping.asDouble }
                .getOrNull()

            CommandArgumentType.BOOLEAN -> runCatching { optionMapping.asBoolean }
                .getOrNull()

            CommandArgumentType.USER -> runCatching { optionMapping.asUser }
                .map { DiscordUser(it) }
                .getOrNull()

            CommandArgumentType.CHANNEL -> runCatching { optionMapping.asChannel }
                .map { DiscordChannel.create(it) }
                .getOrNull()

            CommandArgumentType.ROLE -> runCatching { optionMapping.asRole }
                .map { DiscordRole(it) }
                .getOrNull()

            CommandArgumentType.MENTIONABLE -> runCatching { optionMapping.asMentionable }
                .map { DiscordMentionable.create(it, interaction.jda) }
                .getOrNull()

            CommandArgumentType.ATTACHMENT -> runCatching { optionMapping.asAttachment }
                .map {
                    Attachment(
                        id = it.id,
                        url = it.url,
                        proxyUrl = it.proxyUrl,
                        fileName = it.fileName,
                        manager = DiscordManager.get(interaction.jda)
                    )
                }.getOrNull()
        } as T?
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false

        other as OptionCommandArguments

        if (interaction != other.interaction) return false
        if (defaultKey != other.defaultKey) return false

        return true
    }

    override fun hashCode(): Int = hash(
        interaction,
        defaultKey,
    )

    override fun toString(): String {
        return "OptionCommandArguments(" +
            "interaction=$interaction," +
            "defaultKey=$defaultKey" +
            ")"
    }
}
