package com.shakster.borgar.messaging.entity

import com.shakster.borgar.messaging.command.Command
import kotlinx.coroutines.flow.Flow

interface Guild : ChatRoom {

    val bannerUrl: String?
    val splashUrl: String?
    val maxFileSize: Long
    val publicRole: Role?

    suspend fun getMember(userId: String): Member?

    suspend fun getMember(user: User): Member? = getMember(user.id)

    override suspend fun isMember(userId: String): Boolean =
        getMember(userId) != null

    fun getEmojis(): Flow<CustomEmoji>

    suspend fun addCommand(command: Command)

    suspend fun deleteCommand(commandName: String)
}
