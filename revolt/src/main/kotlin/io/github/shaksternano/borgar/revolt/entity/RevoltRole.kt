package io.github.shaksternano.borgar.revolt.entity

import io.github.shaksternano.borgar.chat.command.Permission
import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.Role
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.revolt.RevoltManager

data class RevoltRole(
    override val manager: RevoltManager,
    override val id: String,
    override val name: String,
    private val permissions: Long,
) : BaseEntity(), Role {

    override val asMention: String = "<@&$id>"
    override val asBasicMention: String = "@$name"

    override suspend fun hasPermission(permissions: Set<Permission>): Boolean = permissions.isEmpty()

    override suspend fun hasPermission(permissions: Set<Permission>, channel: Channel): Boolean = permissions.isEmpty()
}
