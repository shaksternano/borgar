package com.shakster.borgar.stoat.entity

import com.shakster.borgar.messaging.command.Permission
import com.shakster.borgar.messaging.entity.BaseEntity
import com.shakster.borgar.messaging.entity.Role
import com.shakster.borgar.messaging.entity.channel.Channel
import com.shakster.borgar.stoat.StoatManager
import com.shakster.borgar.stoat.entity.channel.StoatChannel
import com.shakster.borgar.stoat.util.StoatPermissionValue
import com.shakster.borgar.stoat.util.bitwiseAndEq
import com.shakster.borgar.stoat.util.getPermissionsValue
import com.shakster.borgar.stoat.util.toValues
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class StoatRole(
    override val manager: StoatManager,
    override val id: String,
    override val name: String,
    val permissionsValue: StoatPermissionValue,
    val rank: Int,
) : Role, Comparable<StoatRole>, BaseEntity() {

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
        if (channel !is StoatChannel) return hasPermission(permissions)
        val permissionsValue = getPermissionsValue(this, channel)
        val toCheck = permissions.toValues()
        return bitwiseAndEq(permissionsValue, toCheck)
    }

    override fun compareTo(other: StoatRole): Int =
        other.rank - rank
}

@Serializable
data class StoatRoleResponse(
    val name: String,
    val permissions: StoatRolePermissionsBody,
    val rank: Int,
) {

    fun convert(
        manager: StoatManager,
        id: String
    ): StoatRole = StoatRole(
        manager = manager,
        id = id,
        name = name,
        permissionsValue = permissions.convert(),
        rank = rank,
    )
}

@Serializable
data class StoatRolePermissionsBody(
    @SerialName("a")
    val allow: Long,
    @SerialName("d")
    val deny: Long,
) {
    fun convert(): StoatPermissionValue =
        StoatPermissionValue(allow, deny)
}
