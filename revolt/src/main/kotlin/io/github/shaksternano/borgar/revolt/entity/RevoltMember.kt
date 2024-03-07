package io.github.shaksternano.borgar.revolt.entity

import io.github.shaksternano.borgar.chat.command.Permission
import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.Member
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.revolt.RevoltManager
import io.github.shaksternano.borgar.revolt.entity.channel.RevoltChannel
import io.github.shaksternano.borgar.revolt.util.bitwiseAndEq
import io.github.shaksternano.borgar.revolt.util.getPermissionsValue
import io.github.shaksternano.borgar.revolt.util.toValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

data class RevoltMember(
    override val manager: RevoltManager,
    override val user: User,
    override val effectiveName: String,
    override val effectiveAvatarUrl: String,
    override val timeoutEnd: OffsetDateTime?,
    private val roleIds: List<String>,
    private val guildId: String,
    private var guild: RevoltGuild? = null,
) : Member, BaseEntity() {

    override val id: String = user.id
    override val roles: Flow<RevoltRole> = roleIds.asFlow()
        .map {
            val response = manager.request<RevoltRoleResponse>("/servers/$guildId/roles/$it")
            response.convert(manager, it)
        }
    override val asMention: String = user.asMention
    override val asBasicMention: String = "@$effectiveName"

    val isTimedOut: Boolean
        get() = timeoutEnd?.isAfter(OffsetDateTime.now()) == true

    override suspend fun getGuild(): RevoltGuild {
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
        if (channel !is RevoltChannel) return hasPermission(permissions)
        val permissionsValue = getPermissionsValue(this, channel)
        val toCheck = permissions.toValues()
        return bitwiseAndEq(permissionsValue, toCheck)
    }
}

@Serializable
data class RevoltMemberResponse(
    @SerialName("_id")
    val id: RevoltMemberIdBody,
    val nickname: String? = null,
    val avatar: RevoltAvatarBody? = null,
    @SerialName("roles")
    val roleIds: List<String> = emptyList(),
    val timeout: String? = null,
) {

    fun convert(
        manager: RevoltManager,
        user: User,
        guild: RevoltGuild? = null,
    ): RevoltMember =
        RevoltMember(
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
data class RevoltMemberIdBody(
    val user: String,
    @SerialName("server")
    val guild: String,
)
