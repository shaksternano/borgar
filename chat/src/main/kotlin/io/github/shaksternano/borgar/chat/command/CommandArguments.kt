package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.entity.Attachment
import io.github.shaksternano.borgar.chat.entity.Mentionable
import io.github.shaksternano.borgar.chat.entity.Role
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.Channel

interface CommandArguments {

    val defaultKey: String?

    fun getString(key: String): String?

    fun getStringList(key: String): List<String>

    fun getLong(key: String): Long?

    fun getLongList(key: String): List<Long>

    fun getDouble(key: String): Double?

    fun getDoubleList(key: String): List<Double>

    fun getBoolean(key: String): Boolean?

    fun getBooleanList(key: String): List<Boolean>

    suspend fun getUser(key: String): User?

    suspend fun getUserList(key: String): List<User>

    suspend fun getChannel(key: String): Channel?

    suspend fun getChannelList(key: String): List<Channel>

    suspend fun getRole(key: String): Role?

    suspend fun getRoleList(key: String): List<Role>

    fun getMentionable(key: String): Mentionable?

    fun getMentionableList(key: String): List<Mentionable>

    fun getAttachment(key: String): Attachment?

    fun getAttachmentList(key: String): List<Attachment>
}
