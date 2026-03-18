package com.shakster.borgar.stoat.entity

import com.shakster.borgar.core.util.encodeUrl
import com.shakster.borgar.messaging.entity.BaseEntity
import com.shakster.borgar.messaging.entity.User
import com.shakster.borgar.stoat.StoatManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class StoatUser(
    override val manager: StoatManager,
    override val id: String,
    override val name: String,
    override val effectiveName: String,
    override val effectiveAvatarUrl: String,
    override val isBot: Boolean,
    val ownerId: String? = null,
) : User, BaseEntity() {

    override val isSelf: Boolean = manager.selfId == id
    override val asMention: String = "<@$id>"
    override val asBasicMention: String = "@$effectiveName"
    override val asSilentMention: String = "<\\@$id>"

    override suspend fun getBannerUrl(): String? = runCatching {
        val response = manager.request<StoatUserProfileResponse>("/users/$id/profile")
        response.background.getUrl(manager)
    }.getOrNull()
}

@Serializable
data class StoatUserResponse(
    @SerialName("_id")
    val id: String,
    val username: String,
    @SerialName("display_name")
    val displayName: String? = null,
    val avatar: StoatAvatarBody? = null,
    val bot: StoatBotBody? = null,
) {
    fun convert(manager: StoatManager): StoatUser =
        StoatUser(
            manager = manager,
            id = id,
            name = username,
            effectiveName = displayName ?: username,
            effectiveAvatarUrl = avatar?.getUrl(manager)
                ?: "${manager.apiUrl}/users/${id}/default_avatar",
            isBot = bot != null,
            ownerId = bot?.ownerId,
        )
}

@Serializable
data class StoatAvatarBody(
    @SerialName("_id")
    val id: String,
    val filename: String,
) {
    fun getUrl(manager: StoatManager): String =
        "${manager.cdnUrl}/avatars/$id/${filename.encodeUrl()}"
}

@Serializable
data class StoatBotBody(
    @SerialName("owner")
    val ownerId: String,
)

@Serializable
data class StoatUserProfileResponse(
    val background: StoatUserProfileBackgroundBody,
)

@Serializable
data class StoatUserProfileBackgroundBody(
    @SerialName("_id")
    val id: String,
    val filename: String,
) {
    fun getUrl(manager: StoatManager): String =
        "${manager.cdnUrl}/backgrounds/$id/${filename.encodeUrl()}"
}
