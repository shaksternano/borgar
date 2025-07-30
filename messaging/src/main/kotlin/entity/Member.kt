package com.shakster.borgar.messaging.entity

import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

interface Member : DisplayedUser, PermissionHolder {

    val user: User
    override val name: String
        get() = user.name
    val roles: Flow<Role>
    val timeoutEnd: OffsetDateTime?

    suspend fun getGuild(): Guild

    suspend fun isOwner(): Boolean =
        id == getGuild().ownerId
}
