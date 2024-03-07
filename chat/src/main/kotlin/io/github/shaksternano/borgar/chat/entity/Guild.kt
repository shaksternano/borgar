package io.github.shaksternano.borgar.chat.entity

import io.github.shaksternano.borgar.chat.command.Command
import kotlinx.coroutines.flow.Flow

interface Guild : ChatRoom {

    val bannerUrl: String?
    val splashUrl: String?
    val maxFileSize: Long
    val publicRole: Role
    val customEmojis: Flow<CustomEmoji>

    suspend fun getMember(userId: String): Member?

    suspend fun getMember(user: User): Member? = getMember(user.id)

    override suspend fun isMember(userId: String): Boolean =
        getMember(userId) != null

    suspend fun addCommand(command: Command)

    suspend fun deleteCommand(commandName: String)
}
