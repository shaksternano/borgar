package com.shakster.borgar.stoat.entity

import com.shakster.borgar.messaging.entity.BaseEntity
import com.shakster.borgar.messaging.entity.Group
import com.shakster.borgar.stoat.StoatManager

data class StoatGroup(
    override val manager: StoatManager,
    override val id: String,
    override val name: String?,
    override val ownerId: String?,
    private val memberIds: Set<String>,
    override val iconUrl: String?,
) : Group, BaseEntity() {

    override suspend fun isMember(userId: String): Boolean =
        memberIds.contains(userId)
}
