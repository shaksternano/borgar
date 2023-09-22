package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.core.util.hash
import io.github.shaksternano.borgar.core.util.kClass
import io.github.shaksternano.borgar.core.util.splitWords
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList

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

    override fun getString(key: String): String? = arguments[key]

    override fun getStringList(key: String): List<String> = arguments[key]?.splitWords() ?: emptyList()

    override fun getLong(key: String): Long? = arguments[key]?.toLongOrNull()

    override fun getLongList(key: String): List<Long> = arguments[key]
        ?.splitWords()
        ?.map { it.toLongOrNull() ?: return emptyList() }
        ?: emptyList()

    override fun getDouble(key: String): Double? = arguments[key]?.toDoubleOrNull()

    override fun getDoubleList(key: String): List<Double> = arguments[key]
        ?.splitWords()
        ?.map { it.toDoubleOrNull() ?: return emptyList() }
        ?: emptyList()

    override fun getBoolean(key: String): Boolean? = arguments[key]?.lowercase()?.toBooleanStrictOrNull()

    override fun getBooleanList(key: String): List<Boolean> = arguments[key]
        ?.splitWords()
        ?.map { it.lowercase().toBooleanStrictOrNull() ?: return emptyList() }
        ?: emptyList()

    override suspend fun getUser(key: String): User? = message.mentionedUsers
        .firstOrNull { it.asMention == arguments[key] }

    override suspend fun getUserList(key: String): List<User> = message.mentionedUsers
        .getMentionableList(key)

    override suspend fun getChannel(key: String): Channel? = message.mentionedChannels
        .firstOrNull { it.asMention == arguments[key] }

    override suspend fun getChannelList(key: String): List<Channel> = message.mentionedChannels
        .getMentionableList(key)

    override suspend fun getRole(key: String): Role? = message.mentionedRoles
        .firstOrNull { it.asMention == arguments[key] }

    override suspend fun getRoleList(key: String): List<Role> = message.mentionedRoles
        .getMentionableList(key)

    override fun getMentionable(key: String): Mentionable? {
        val mentions = message.mentionedUserIds + message.mentionedChannelIds + message.mentionedRoleIds
        return mentions.firstOrNull {
            it.asMention == arguments[key]
        }?.let { return it }
    }

    override fun getMentionableList(key: String): List<Mentionable> {
        val mentions = message.mentionedUserIds + message.mentionedChannelIds + message.mentionedRoleIds
        return mentions.zip(key.splitWords())
            .map { (mentionable, mentionableString) ->
                if (mentionable.asMention == mentionableString) mentionable
                else return emptyList()
            }
    }

    override fun getAttachment(key: String): Attachment? = message.attachments.firstOrNull()

    override fun getAttachmentList(key: String): List<Attachment> = message.attachments

    private suspend fun <T : Mentionable> Flow<T>.getMentionableList(key: String): List<T> {
        val mentions = arguments[key]?.splitWords()?.toSet() ?: return emptyList()
        return matchAll { mentions.contains(it.asMention) }
    }

    private suspend fun <T> Flow<T>.matchAll(predicate: suspend (T) -> Boolean): List<T> {
        var invalid = false
        val list = takeWhile {
            if (predicate(it)) true
            else {
                invalid = true
                false
            }
        }.toList()
        return if (invalid) emptyList() else list
    }

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
