package io.github.shaksternano.borgar.revolt.entity

import io.github.shaksternano.borgar.core.util.encodeUrl
import io.github.shaksternano.borgar.messaging.entity.BaseEntity
import io.github.shaksternano.borgar.messaging.entity.User
import io.github.shaksternano.borgar.revolt.RevoltManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class RevoltUser(
    override val manager: RevoltManager,
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

    override suspend fun getBannerUrl(): String? = runCatching {
        val response = manager.request<RevoltUserProfileResponse>("/users/$id/profile")
        response.background.getUrl(manager)
    }.getOrNull()
}

@Serializable
data class RevoltUserResponse(
    @SerialName("_id")
    val id: String,
    val username: String,
    @SerialName("display_name")
    val displayName: String? = null,
    val avatar: RevoltAvatarBody? = null,
    val bot: RevoltBotBody? = null,
) {
    fun convert(manager: RevoltManager): RevoltUser =
        RevoltUser(
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
data class RevoltAvatarBody(
    @SerialName("_id")
    val id: String,
    val filename: String,
) {
    fun getUrl(manager: RevoltManager): String =
        "${manager.cdnUrl}/avatars/$id/${filename.encodeUrl()}"
}

@Serializable
data class RevoltBotBody(
    @SerialName("owner")
    val ownerId: String,
)

@Serializable
data class RevoltUserProfileResponse(
    val background: RevoltUserProfileBackgroundBody,
)

@Serializable
data class RevoltUserProfileBackgroundBody(
    @SerialName("_id")
    val id: String,
    val filename: String,
) {
    fun getUrl(manager: RevoltManager): String =
        "${manager.cdnUrl}/backgrounds/$id/${filename.encodeUrl()}"
}
