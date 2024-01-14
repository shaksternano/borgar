package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.Attachment
import io.github.shaksternano.borgar.chat.entity.Mentionable
import io.github.shaksternano.borgar.chat.entity.Role
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.Channel

interface CommandArguments {

    val defaultKey: String?

    fun hasKey(key: String): Boolean

    fun getString(key: String): String?

    fun getLong(key: String): Long?

    fun getDouble(key: String): Double?

    fun getBoolean(key: String): Boolean?

    suspend fun getUser(key: String): User?

    suspend fun getChannel(key: String): Channel?

    suspend fun getRole(key: String): Role?

    fun getMentionable(key: String): Mentionable?

    fun getAttachment(key: String): Attachment?
}

fun CommandArguments.getDefaultStringOrEmpty(): String = defaultKey?.let(::getString) ?: ""

fun CommandArguments.getDefaultAttachment(): Attachment? = getAttachment("file")

fun CommandArguments.getDefaultUrl(): String? = getString("url")
