package io.github.shaksternano.borgar.revolt.entity

import io.github.shaksternano.borgar.chat.command.Command
import io.github.shaksternano.borgar.chat.entity.*
import io.github.shaksternano.borgar.revolt.RevoltManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class RevoltGuild(
    override val manager: RevoltManager,
    override val id: String,
    override val name: String,
    override val iconUrl: String?,
    override val bannerUrl: String?,
    override val publicRole: Role,
) : BaseEntity(), Guild {

    override val splashUrl: String? = null
    override val maxFileSize: Long = manager.maxFileSize

    override suspend fun getMember(userId: String): RevoltMember? {
        val user = manager.getUser(userId) ?: return null
        return getMember(user)
    }

    override suspend fun getMember(user: User): RevoltMember? =
        runCatching {
            manager.request<RevoltMemberResponse>("/servers/$id/members/${user.id}")
        }.getOrNull()?.convert(manager, user, this)

    override suspend fun getCustomEmojis(): List<CustomEmoji> = emptyList()

    override suspend fun addCommand(command: Command) = Unit

    override suspend fun deleteCommand(commandName: String) = Unit
}

@Serializable
data class RevoltGuildResponse(
    @SerialName("_id")
    val id: String,
    val name: String,
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
            iconUrl = icon?.getUrl(manager),
            bannerUrl = banner?.getUrl(manager),
            publicRole = RevoltRole(
                manager = manager,
                id = id,
                name = "everyone",
                permissions = defaultPermissions,
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
        "${manager.cdnDomain}/icons/$id/$filename"
}

@Serializable
data class RevoltBannerBody(
    @SerialName("_id")
    val id: String,
    val filename: String,
) {

    fun getUrl(manager: RevoltManager): String =
        "${manager.cdnDomain}/banners/$id/$filename"
}
