package io.github.shaksternano.borgar.chat.entity

interface Guild : Entity {

    val name: String
    val iconUrl: String?
    val bannerUrl: String?

    suspend fun getMember(userId: String): Member?

    suspend fun getMember(user: User): Member? = getMember(user.id)

    suspend fun isMember(userId: String): Boolean =
        getMember(userId) != null

    suspend fun isMember(user: User): Boolean =
        isMember(user.id)

    suspend fun getCustomEmojis(): List<CustomEmoji>

    suspend fun getMaxFileSize(): Long

    suspend fun getPublicRole(): Role
}
