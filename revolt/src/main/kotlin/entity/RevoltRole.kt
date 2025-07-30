package com.shakster.borgar.revolt.entity

import com.shakster.borgar.messaging.command.Permission
import com.shakster.borgar.messaging.entity.BaseEntity
import com.shakster.borgar.messaging.entity.Role
import com.shakster.borgar.messaging.entity.channel.Channel
import com.shakster.borgar.revolt.RevoltManager
import com.shakster.borgar.revolt.entity.channel.RevoltChannel
import com.shakster.borgar.revolt.util.RevoltPermissionValue
import com.shakster.borgar.revolt.util.bitwiseAndEq
import com.shakster.borgar.revolt.util.getPermissionsValue
import com.shakster.borgar.revolt.util.toValues
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class RevoltRole(
    override val manager: RevoltManager,
    override val id: String,
    override val name: String,
    val permissionsValue: RevoltPermissionValue,
    val rank: Int,
) : Role, Comparable<RevoltRole>, BaseEntity() {

    override val asMention: String = "<@&$id>"
    override val asBasicMention: String = "@$name"

    override suspend fun hasPermission(permissions: Set<Permission>): Boolean {
        if (permissions.isEmpty()) return true
        val permissionsValue = getPermissionsValue(this)
        val toCheck = permissions.toValues()
        return bitwiseAndEq(permissionsValue, toCheck)
    }

    override suspend fun hasPermission(permissions: Set<Permission>, channel: Channel): Boolean {
        if (permissions.isEmpty()) return true
        if (channel !is RevoltChannel) return hasPermission(permissions)
        val permissionsValue = getPermissionsValue(this, channel)
        val toCheck = permissions.toValues()
        return bitwiseAndEq(permissionsValue, toCheck)
    }

    override fun compareTo(other: RevoltRole): Int =
        other.rank - rank
}

@Serializable
data class RevoltRoleResponse(
    val name: String,
    val permissions: RevoltRolePermissionsBody,
    val rank: Int,
) {

    fun convert(
        manager: RevoltManager,
        id: String
    ): RevoltRole = RevoltRole(
        manager = manager,
        id = id,
        name = name,
        permissionsValue = permissions.convert(),
        rank = rank,
    )
}

@Serializable
data class RevoltRolePermissionsBody(
    @SerialName("a")
    val allow: Long,
    @SerialName("d")
    val deny: Long,
) {
    fun convert(): RevoltPermissionValue =
        RevoltPermissionValue(allow, deny)
}
