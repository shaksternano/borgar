package com.shakster.borgar.discord.command

import com.shakster.borgar.core.util.formatted
import com.shakster.borgar.core.util.hash
import com.shakster.borgar.core.util.kClass
import com.shakster.borgar.discord.DiscordManager
import com.shakster.borgar.discord.entity.DiscordMentionable
import com.shakster.borgar.discord.entity.DiscordRole
import com.shakster.borgar.discord.entity.DiscordUser
import com.shakster.borgar.discord.entity.channel.DiscordChannel
import com.shakster.borgar.messaging.command.ARGUMENT_PREFIX
import com.shakster.borgar.messaging.command.CommandArgumentType
import com.shakster.borgar.messaging.command.CommandArguments
import com.shakster.borgar.messaging.command.SimpleCommandArgumentType
import com.shakster.borgar.messaging.entity.Attachment
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionType

class DiscordOptionCommandArguments(
    private val interaction: CommandInteractionPayload,
    override val defaultKey: String?,
) : CommandArguments {

    override val typedForm: String = interaction.options
        .filter {
            it.name != AFTER_COMMANDS_ARGUMENT
        }
        .joinToString(" ") {
            val formatted =
                if (it.type == OptionType.NUMBER) it.asDouble.formatted
                else it.asString
            "$ARGUMENT_PREFIX${it.name} $formatted"
        }

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
            CommandArgumentType.String -> optionMapping.asString

            CommandArgumentType.Integer -> runCatching { optionMapping.asLong.toInt() }
                .getOrNull()

            CommandArgumentType.Long -> runCatching { optionMapping.asLong }
                .getOrNull()

            CommandArgumentType.Double -> runCatching { optionMapping.asDouble }
                .getOrNull()

            CommandArgumentType.Boolean -> runCatching { optionMapping.asBoolean }
                .getOrNull()

            CommandArgumentType.User -> runCatching {
                val user = optionMapping.asUser
                DiscordUser(user)
            }.getOrNull()

            CommandArgumentType.Channel -> runCatching {
                val channel = optionMapping.asChannel
                DiscordChannel.create(channel, interaction.context)
            }.getOrNull()

            CommandArgumentType.Role -> runCatching {
                val role = optionMapping.asRole
                DiscordRole(role)
            }.getOrNull()

            CommandArgumentType.Mentionable -> runCatching {
                val mentionable = optionMapping.asMentionable
                DiscordMentionable.create(mentionable, interaction.jda, interaction.context)
            }.getOrNull()

            CommandArgumentType.Attachment -> runCatching {
                val attachment = optionMapping.asAttachment
                Attachment(
                    id = attachment.id,
                    url = attachment.url,
                    proxyUrl = attachment.proxyUrl,
                    filename = attachment.fileName,
                    manager = DiscordManager[interaction.jda]
                )
            }.getOrNull()

            is CommandArgumentType.Enum<*> -> runCatching {
                val ordinal = optionMapping.asLong.toInt()
                argumentType.values[ordinal]
            }.getOrNull()
        } as T?
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false

        other as DiscordOptionCommandArguments

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
