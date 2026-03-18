package com.shakster.borgar.stoat.entity

import com.shakster.borgar.core.util.encodeUrl
import com.shakster.borgar.messaging.command.Command
import com.shakster.borgar.messaging.entity.BaseEntity
import com.shakster.borgar.messaging.entity.CustomEmoji
import com.shakster.borgar.messaging.entity.Guild
import com.shakster.borgar.messaging.entity.User
import com.shakster.borgar.stoat.StoatManager
import com.shakster.borgar.stoat.util.StoatPermissionValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class StoatGuild(
    override val id: String,
    override val name: String,
    override val ownerId: String,
    override val iconUrl: String?,
    override val bannerUrl: String?,
    override val publicRole: StoatRole,
    override val manager: StoatManager,
) : Guild, BaseEntity() {

    override val splashUrl: String? = null
    override val maxFileSize: Long = manager.maxFileSize

    private val memberCache: MutableMap<String, StoatMember> = mutableMapOf()

    override suspend fun getMember(userId: String): StoatMember? {
        memberCache[userId]?.let {
            return it
        }
        val user = manager.getUser(userId) ?: return null
        return getMember(user)
    }

    override suspend fun getMember(user: User): StoatMember? {
        val userId = user.id
        memberCache[userId]?.let {
            return it
        }
        return runCatching {
            manager.request<StoatMemberResponse>("/servers/$id/members/$userId")
        }.getOrNull()?.convert(manager, user, this)?.also {
            memberCache[userId] = it
        }
    }

    override fun getEmojis(): Flow<CustomEmoji> {
        return flow {
            manager.request<List<StoatEmojiResponse>>("/servers/$id/emojis")
                .forEach {
                    val emoji = it.convert(manager)
                    emit(emoji)
                }
        }
    }

    override suspend fun addCommand(command: Command) = Unit

    override suspend fun deleteCommand(commandName: String) = Unit
}

@Serializable
data class StoatGuildResponse(
    @SerialName("_id")
    val id: String,
    val name: String,
    @SerialName("owner")
    val ownerId: String,
    val icon: StoatIconBody? = null,
    val banner: StoatBannerBody? = null,
    @SerialName("default_permissions")
    val defaultPermissions: Long,
) {
    fun convert(manager: StoatManager): StoatGuild =
        StoatGuild(
            id = id,
            name = name,
            ownerId = ownerId,
            iconUrl = icon?.getUrl(manager),
            bannerUrl = banner?.getUrl(manager),
            publicRole = StoatRole(
                manager = manager,
                id = id,
                name = "everyone",
                permissionsValue = StoatPermissionValue(
                    allowed = defaultPermissions,
                    denied = 0,
                ),
                rank = Int.MAX_VALUE,
            ),
            manager = manager,
        )
}

@Serializable
data class StoatIconBody(
    @SerialName("_id")
    val id: String,
    val filename: String,
) {
    fun getUrl(manager: StoatManager): String =
        "${manager.cdnUrl}/icons/$id/${filename.encodeUrl()}"
}

@Serializable
data class StoatBannerBody(
    @SerialName("_id")
    val id: String,
    val filename: String,
) {
    fun getUrl(manager: StoatManager): String =
        "${manager.cdnUrl}/banners/$id/${filename.encodeUrl()}"
}
