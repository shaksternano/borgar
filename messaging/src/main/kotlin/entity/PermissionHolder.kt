package com.shakster.borgar.messaging.entity

import com.shakster.borgar.messaging.command.Permission
import com.shakster.borgar.messaging.entity.channel.Channel

interface PermissionHolder : Entity {

    suspend fun hasPermission(permissions: Set<Permission>): Boolean

    suspend fun hasPermission(permissions: Set<Permission>, channel: Channel): Boolean
}
