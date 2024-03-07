package io.github.shaksternano.borgar.revolt.entity

import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.Group
import io.github.shaksternano.borgar.revolt.RevoltManager

data class RevoltGroup(
    override val manager: RevoltManager,
    override val id: String,
    override val name: String,
    override val ownerId: String,
    private val memberIds: Set<String>,
    override val iconUrl: String?,
): Group, BaseEntity() {

    override suspend fun isMember(userId: String): Boolean =
        memberIds.contains(userId)
}
