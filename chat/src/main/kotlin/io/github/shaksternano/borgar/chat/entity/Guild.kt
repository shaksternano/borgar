package io.github.shaksternano.borgar.chat.entity

interface Guild : Entity {

    suspend fun getMember(userId: String): Member?

    suspend fun getMember(user: User): Member? = getMember(user.id)

    suspend fun isMember(userId: String): Boolean =
        getMember(userId) != null

    suspend fun isMember(user: User): Boolean =
        isMember(user.id)

    fun getCustomEmojis(): List<CustomEmoji>

    suspend fun getMaxFileSize(): Long
}
