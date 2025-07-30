package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.collect.plus
import com.shakster.borgar.core.util.Displayed
import com.shakster.borgar.core.util.Identified
import com.shakster.borgar.messaging.entity.Message
import kotlinx.coroutines.flow.firstOrNull
import kotlin.reflect.KClass

sealed interface CommandArgumentType<T> {

    data object String : SimpleCommandArgumentType<kotlin.String> {

        override val name: kotlin.String = "string"

        override fun parse(value: kotlin.String, message: Message): kotlin.String = value
    }

    data object Integer : SimpleCommandArgumentType<Int> {

        override val name: kotlin.String = "integer"

        override fun parse(value: kotlin.String, message: Message): Int? =
            Long.parse(value, message)?.toInt()
    }

    data object Long : SimpleCommandArgumentType<kotlin.Long> {

        override val name: kotlin.String = "integer"

        override fun parse(value: kotlin.String, message: Message): kotlin.Long? = runCatching {
            java.lang.Long.decode(value)
        }.getOrNull()
    }

    data object Double : SimpleCommandArgumentType<kotlin.Double> {

        override val name: kotlin.String = "number"

        override fun parse(value: kotlin.String, message: Message): kotlin.Double? =
            value.toDoubleOrNull()
    }

    data object Boolean : SimpleCommandArgumentType<kotlin.Boolean> {

        override val name: kotlin.String = "boolean"

        override fun parse(value: kotlin.String, message: Message): kotlin.Boolean? =
            value.lowercase().toBooleanStrictOrNull()
    }

    data object User : SuspendingCommandArgumentType<com.shakster.borgar.messaging.entity.User> {

        override val name: kotlin.String = "user"

        override suspend fun parse(
            value: kotlin.String,
            message: Message,
        ): com.shakster.borgar.messaging.entity.User? =
            message.mentionedUsers.firstOrNull {
                value == it.asMention || value == it.asBasicMention
            }
    }

    data object Channel :
        SuspendingCommandArgumentType<com.shakster.borgar.messaging.entity.channel.Channel> {

        override val name: kotlin.String = "channel"

        override suspend fun parse(
            value: kotlin.String,
            message: Message,
        ): com.shakster.borgar.messaging.entity.channel.Channel? =
            message.mentionedChannels.firstOrNull {
                value == it.asMention || value == it.asBasicMention
            }
    }

    data object Role : SuspendingCommandArgumentType<com.shakster.borgar.messaging.entity.Role> {

        override val name: kotlin.String = "role"

        override suspend fun parse(
            value: kotlin.String,
            message: Message
        ): com.shakster.borgar.messaging.entity.Role? =
            message.mentionedRoles.firstOrNull {
                value == it.asMention || value == it.asBasicMention
            }
    }

    data object Mentionable :
        SuspendingCommandArgumentType<com.shakster.borgar.messaging.entity.Mentionable> {

        override val name: kotlin.String = "mentionable"

        override suspend fun parse(
            value: kotlin.String,
            message: Message,
        ): com.shakster.borgar.messaging.entity.Mentionable? {
            val mentions = message.mentionedUsers + message.mentionedChannels + message.mentionedRoles
            return mentions.firstOrNull {
                value == it.asMention || value == it.asBasicMention
            }
        }
    }

    data object Attachment : SimpleCommandArgumentType<com.shakster.borgar.messaging.entity.Attachment> {

        override val name: kotlin.String = "attachment"

        override fun parse(
            value: kotlin.String,
            message: Message,
        ): com.shakster.borgar.messaging.entity.Attachment? =
            message.attachments.firstOrNull()
    }

    class Enum<T>(
        private val type: KClass<T>,
        override val name: kotlin.String,
    ) : SimpleCommandArgumentType<T> where T : kotlin.Enum<T>, T : Identified, T : Displayed {

        val values: List<T> = type.java.enumConstants.toList()

        override fun parse(value: kotlin.String, message: Message): T? =
            type.java.enumConstants.firstOrNull { value == it.id }
    }

    val name: kotlin.String

    companion object
}

inline fun <reified T> CommandArgumentType.Companion.Enum(
    name: String,
): CommandArgumentType.Enum<T> where T : Enum<T>, T : Identified, T : Displayed {
    return CommandArgumentType.Enum(T::class, name)
}

sealed interface SimpleCommandArgumentType<T> : CommandArgumentType<T> {

    fun parse(value: String, message: Message): T?
}

sealed interface SuspendingCommandArgumentType<T> : CommandArgumentType<T> {

    suspend fun parse(value: String, message: Message): T?
}
