package io.github.shaksternano.borgar.revolt.entity

import io.github.shaksternano.borgar.core.util.encodeUrl
import io.github.shaksternano.borgar.messaging.command.Command
import io.github.shaksternano.borgar.messaging.entity.BaseEntity
import io.github.shaksternano.borgar.messaging.entity.CustomEmoji
import io.github.shaksternano.borgar.messaging.entity.Guild
import io.github.shaksternano.borgar.messaging.entity.User
import io.github.shaksternano.borgar.revolt.RevoltManager
import io.github.shaksternano.borgar.revolt.util.RevoltPermissionValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class RevoltGuild(
    override val manager: RevoltManager,
    override val id: String,
    override val name: String,
    override val ownerId: String,
    override val iconUrl: String?,
    override val bannerUrl: String?,
    override val publicRole: RevoltRole,
) : Guild, BaseEntity() {

    override val splashUrl: String? = null
    override val maxFileSize: Long = manager.maxFileSize
    override val customEmojis: Flow<CustomEmoji> = flow {
        val response = manager.request<List<RevoltEmojiResponse>>("/servers/$id/emojis")
        response.forEach {
            val emoji = it.convert(manager)
            emit(emoji)
        }
    }

    private val memberCache: MutableMap<String, RevoltMember> = mutableMapOf()

    override suspend fun getMember(userId: String): RevoltMember? {
        memberCache[userId]?.let {
            return it
        }
        val user = manager.getUser(userId) ?: return null
        return getMember(user)
    }

    override suspend fun getMember(user: User): RevoltMember? {
        val userId = user.id
        memberCache[userId]?.let {
            return it
        }
        return runCatching {
            manager.request<RevoltMemberResponse>("/servers/$id/members/$userId")
        }.getOrNull()?.convert(manager, user, this)?.also {
            memberCache[userId] = it
        }
    }

    override suspend fun addCommand(command: Command) = Unit

    override suspend fun deleteCommand(commandName: String) = Unit
}

@Serializable
data class RevoltGuildResponse(
    @SerialName("_id")
    val id: String,
    val name: String,
    @SerialName("owner")
    val ownerId: String,
    val icon: RevoltIconBody? = null,
    val banner: RevoltBannerBody? = null,
    @SerialName("default_permissions")
    val defaultPermissions: Long,
) {

    fun convert(manager: RevoltManager): RevoltGuild =
        RevoltGuild(
            manager = manager,
            id = id,
            name = name,
            ownerId = ownerId,
            iconUrl = icon?.getUrl(manager),
            bannerUrl = banner?.getUrl(manager),
            publicRole = RevoltRole(
                manager = manager,
                id = id,
                name = "everyone",
                permissionsValue = RevoltPermissionValue(
                    allowed = defaultPermissions,
                    denied = 0,
                ),
                rank = Int.MAX_VALUE,
            ),
        )
}

@Serializable
data class RevoltIconBody(
    @SerialName("_id")
    val id: String,
    val filename: String,
) {

    fun getUrl(manager: RevoltManager): String =
        "${manager.cdnDomain}/icons/$id/${filename.encodeUrl()}"
}

@Serializable
data class RevoltBannerBody(
    @SerialName("_id")
    val id: String,
    val filename: String,
) {

    fun getUrl(manager: RevoltManager): String =
        "${manager.cdnDomain}/banners/$id/${filename.encodeUrl()}"
}
