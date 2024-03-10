package io.github.shaksternano.borgar.messaging.entity

import io.github.shaksternano.borgar.messaging.command.Permission
import io.github.shaksternano.borgar.messaging.entity.channel.Channel

interface PermissionHolder : Entity {

    suspend fun hasPermission(permissions: Set<Permission>): Boolean

    suspend fun hasPermission(permissions: Set<Permission>, channel: Channel): Boolean
}
