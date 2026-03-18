package com.shakster.borgar.stoat.entity

import com.shakster.borgar.messaging.command.Permission
import com.shakster.borgar.messaging.entity.BaseEntity
import com.shakster.borgar.messaging.entity.Member
import com.shakster.borgar.messaging.entity.User
import com.shakster.borgar.messaging.entity.channel.Channel
import com.shakster.borgar.stoat.StoatManager
import com.shakster.borgar.stoat.entity.channel.StoatChannel
import com.shakster.borgar.stoat.util.bitwiseAndEq
import com.shakster.borgar.stoat.util.getPermissionsValue
import com.shakster.borgar.stoat.util.toValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

data class StoatMember(
    override val manager: StoatManager,
    override val user: User,
    override val effectiveName: String,
    override val effectiveAvatarUrl: String,
    override val timeoutEnd: OffsetDateTime?,
    private val roleIds: List<String>,
    private val guildId: String,
    private var guild: StoatGuild? = null,
) : Member, BaseEntity() {

    override val id: String = user.id
    override val roles: Flow<StoatRole> = roleIds.asFlow()
        .map {
            val response = manager.request<StoatRoleResponse>("/servers/$guildId/roles/$it")
            response.convert(manager, it)
        }
    override val asMention: String = user.asMention
    override val asBasicMention: String = "@$effectiveName"

    val isTimedOut: Boolean
        get() = timeoutEnd?.isAfter(OffsetDateTime.now()) == true

    override suspend fun getGuild(): StoatGuild {
        guild?.let { return it }
        return manager.getGuild(guildId).also {
            guild = it
        } ?: error("Guild $guildId not found")
    }

    override suspend fun hasPermission(permissions: Set<Permission>): Boolean {
        if (permissions.isEmpty() || isOwner()) return true
        val permissionsValue = getPermissionsValue(this)
        val toCheck = permissions.toValues()
        return bitwiseAndEq(permissionsValue, toCheck)
    }

    override suspend fun hasPermission(permissions: Set<Permission>, channel: Channel): Boolean {
        if (permissions.isEmpty() || isOwner()) return true
        if (channel !is StoatChannel) return hasPermission(permissions)
        val permissionsValue = getPermissionsValue(this, channel)
        val toCheck = permissions.toValues()
        return bitwiseAndEq(permissionsValue, toCheck)
    }
}

@Serializable
data class StoatMemberResponse(
    @SerialName("_id")
    val id: StoatMemberIdBody,
    val nickname: String? = null,
    val avatar: StoatAvatarBody? = null,
    @SerialName("roles")
    val roleIds: List<String> = emptyList(),
    val timeout: String? = null,
) {

    fun convert(
        manager: StoatManager,
        user: User,
        guild: StoatGuild? = null,
    ): StoatMember =
        StoatMember(
            manager = manager,
            user = user,
            effectiveName = nickname ?: user.effectiveName,
            effectiveAvatarUrl = avatar?.getUrl(manager) ?: user.effectiveAvatarUrl,
            timeoutEnd = timeout?.let { OffsetDateTime.parse(it) },
            roleIds = roleIds,
            guildId = id.guild,
            guild = guild,
        )
}

@Serializable
data class StoatMemberIdBody(
    val user: String,
    @SerialName("server")
    val guild: String,
)
