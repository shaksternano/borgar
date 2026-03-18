package com.shakster.borgar.stoat.util

import com.shakster.borgar.core.util.pow
import com.shakster.borgar.messaging.command.Permission
import com.shakster.borgar.stoat.entity.StoatMember
import com.shakster.borgar.stoat.entity.StoatRole
import com.shakster.borgar.stoat.entity.channel.StoatChannel
import kotlinx.coroutines.flow.toList

enum class StoatPermission(
    val displayName: String,
    val value: Long,
) {
    MANAGE_CUSTOMISATION("Manage Customisation", 2 pow 4),
    VIEW_CHANNELS("View Channel", 2 pow 20),
    READ_MESSAGE_HISTORY("Read Message History", 2 pow 21),
    ;

    companion object {
        val ALL_VALUE: Long = 2 pow 52
        val ALLOW_IN_TIMEOUT_VALUE: Long = VIEW_CHANNELS.value + READ_MESSAGE_HISTORY.value
    }
}

data class StoatPermissionValue(
    val allowed: Long,
    val denied: Long,
) {
    fun applyToValue(permissionsValue: Long): Long {
        return permissionsValue or allowed and denied.inv()
    }
}

fun Permission.toStoat(): StoatPermission = when (this) {
    Permission.MANAGE_GUILD_EXPRESSIONS -> StoatPermission.MANAGE_CUSTOMISATION
}

fun Iterable<Permission>.toValues(): List<Long> =
    map { it.toStoat().value }

suspend fun getPermissionsValue(member: StoatMember): Long {
    if (member.isOwner()) return StoatPermission.ALL_VALUE

    val guild = member.getGuild()
    var permissionsValue = guild.publicRole.permissionsValue.allowed

    val orderedRoles = member.roles.toList().sorted()
    orderedRoles.forEach {
        permissionsValue = it.permissionsValue.applyToValue(permissionsValue)
    }

    if (member.isTimedOut) {
        permissionsValue = permissionsValue and StoatPermission.ALLOW_IN_TIMEOUT_VALUE
    }

    return permissionsValue
}

suspend fun getPermissionsValue(member: StoatMember, channel: StoatChannel): Long {
    if (member.isOwner()) return StoatPermission.ALL_VALUE

    var permissionsValue = getPermissionsValue(member)
    channel.defaultPermissions?.let {
        permissionsValue = it.applyToValue(permissionsValue)
    }

    val orderedRoles = member.roles.toList().sorted()
    orderedRoles.forEach { role ->
        val override = channel.rolePermissions[role.id]
        override?.let {
            permissionsValue = it.applyToValue(permissionsValue)
        }
    }

    if (member.isTimedOut) {
        permissionsValue = permissionsValue and StoatPermission.ALLOW_IN_TIMEOUT_VALUE
    }

    return permissionsValue
}

fun getPermissionsValue(role: StoatRole): Long =
    role.permissionsValue.applyToValue(0)

fun getPermissionsValue(role: StoatRole, channel: StoatChannel): Long {
    var permissionsValue = getPermissionsValue(role)
    channel.defaultPermissions?.let {
        permissionsValue = it.applyToValue(permissionsValue)
    }

    val override = channel.rolePermissions[role.id]
    override?.let {
        permissionsValue = it.applyToValue(permissionsValue)
    }

    return permissionsValue
}

fun bitwiseAndEq(a: Long, b: Iterable<Long>): Boolean {
    val value = b.fold(0L) { previous, current ->
        previous or current
    }
    return value and a == value
}
