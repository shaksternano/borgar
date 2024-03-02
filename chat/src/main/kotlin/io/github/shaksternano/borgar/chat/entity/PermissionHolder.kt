package io.github.shaksternano.borgar.chat.entity

import io.github.shaksternano.borgar.chat.command.Permission
import io.github.shaksternano.borgar.chat.entity.channel.Channel

interface PermissionHolder : Entity {

    suspend fun hasPermission(permissions: Iterable<Permission>): Boolean

    suspend fun hasPermission(permissions: Iterable<Permission>, channel: Channel): Boolean
}
