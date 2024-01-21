package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import kotlinx.coroutines.flow.firstOrNull

sealed interface CommandArgumentType<T> {

    data object STRING : SimpleCommandArgumentType<String> {

        override val name: String = "string"

        override fun parse(value: String, message: Message): String = value
    }

    data object INTEGER : SimpleCommandArgumentType<Int> {

        override val name: String = "integer"

        override fun parse(value: String, message: Message): Int? =
            LONG.parse(value, message)?.toInt()
    }

    data object LONG : SimpleCommandArgumentType<Long> {

        override val name: String = "integer"

        override fun parse(value: String, message: Message): Long? = runCatching {
            java.lang.Long.decode(value)
        }.getOrNull()
    }

    data object DOUBLE : SimpleCommandArgumentType<Double> {

        override val name: String = "number"

        override fun parse(value: String, message: Message): Double? =
            value.toDoubleOrNull()
    }

    data object BOOLEAN : SimpleCommandArgumentType<Boolean> {

        override val name: String = "boolean"

        override fun parse(value: String, message: Message): Boolean? =
            value.lowercase().toBooleanStrictOrNull()
    }

    data object USER : SuspendingCommandArgumentType<User> {

        override val name: String = "user"

        override suspend fun parse(value: String, message: Message): User? =
            message.mentionedUsers.firstOrNull { it.asMention == value }
    }

    data object CHANNEL : SuspendingCommandArgumentType<Channel> {

        override val name: String = "channel"

        override suspend fun parse(value: String, message: Message): Channel? =
            message.mentionedChannels.firstOrNull { it.asMention == value }
    }

    data object ROLE : SuspendingCommandArgumentType<Role> {

        override val name: String = "role"

        override suspend fun parse(value: String, message: Message): Role? =
            message.mentionedRoles.firstOrNull { it.asMention == value }
    }

    data object MENTIONABLE : SimpleCommandArgumentType<Mentionable> {

        override val name: String = "mentionable"

        override fun parse(value: String, message: Message): Mentionable? {
            val mentions = message.mentionedUserIds + message.mentionedChannelIds + message.mentionedRoleIds
            return mentions.firstOrNull {
                it.asMention == value
            }
        }
    }

    data object ATTACHMENT : SimpleCommandArgumentType<Attachment> {

        override val name: String = "attachment"

        override fun parse(value: String, message: Message): Attachment? =
            message.attachments.firstOrNull()
    }

    val name: String
}

sealed interface SimpleCommandArgumentType<T> : CommandArgumentType<T> {

    fun parse(value: String, message: Message): T?
}

sealed interface SuspendingCommandArgumentType<T> : CommandArgumentType<T> {

    suspend fun parse(value: String, message: Message): T?
}
