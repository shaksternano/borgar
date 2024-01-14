package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.core.util.hash
import io.github.shaksternano.borgar.core.util.kClass
import kotlinx.coroutines.flow.firstOrNull

class MessageCommandArguments(
    arguments: Map<String, String>,
    defaultArgument: String,
    override val defaultKey: String?,
    private val message: Message,
) : CommandArguments {

    private val arguments: Map<String, String> = buildMap {
        if (defaultKey != null) {
            put(defaultKey, defaultArgument)
        }
        putAll(arguments)
    }

    override fun hasKey(key: String): Boolean =
        arguments.containsKey(key)

    override fun getString(key: String): String? = arguments[key]

    override fun getLong(key: String): Long? = arguments[key]?.toLongOrNull()

    override fun getDouble(key: String): Double? = arguments[key]?.toDoubleOrNull()

    override fun getBoolean(key: String): Boolean? = arguments[key]?.lowercase()?.toBooleanStrictOrNull()

    override suspend fun getUser(key: String): User? = message.mentionedUsers
        .firstOrNull { it.asMention == arguments[key] }

    override suspend fun getChannel(key: String): Channel? = message.mentionedChannels
        .firstOrNull { it.asMention == arguments[key] }

    override suspend fun getRole(key: String): Role? = message.mentionedRoles
        .firstOrNull { it.asMention == arguments[key] }

    override fun getMentionable(key: String): Mentionable? {
        val mentions = message.mentionedUserIds + message.mentionedChannelIds + message.mentionedRoleIds
        return mentions.firstOrNull {
            it.asMention == arguments[key]
        }?.let { return it }
    }

    override fun getAttachment(key: String): Attachment? = message.attachments.firstOrNull()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (kClass != other?.kClass) return false
        other as MessageCommandArguments
        if (defaultKey != other.defaultKey) return false
        if (message != other.message) return false
        if (arguments != other.arguments) return false
        return true
    }

    override fun hashCode(): Int = hash(
        defaultKey,
        message,
        arguments
    )
}
