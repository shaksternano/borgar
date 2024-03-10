package io.github.shaksternano.borgar.messaging.entity

interface ChatRoom : Entity {

    val name: String
    val ownerId: String
    val iconUrl: String?

    suspend fun isMember(userId: String): Boolean

    suspend fun isMember(user: User): Boolean =
        isMember(user.id)
}
