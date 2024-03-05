package io.github.shaksternano.borgar.revolt.entity

import io.github.shaksternano.borgar.chat.command.Permission
import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Member
import io.github.shaksternano.borgar.chat.entity.User
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.revolt.RevoltManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class RevoltMember(
    override val manager: RevoltManager,
    override val user: User,
    override val effectiveName: String,
    override val effectiveAvatarUrl: String,
    private val guildId: String,
    private var guild: Guild? = null,
) : BaseEntity(), Member {

    override val id: String = user.id
    override val asMention: String = user.asMention
    override val asBasicMention: String = "@$effectiveName"

    override suspend fun getGuild(): Guild {
        guild?.let { return it }
        return manager.getGuild(guildId).also {
            guild = it
        } ?: error("Guild $guildId not found")
    }

    override suspend fun hasPermission(permissions: Set<Permission>): Boolean = permissions.isEmpty()

    override suspend fun hasPermission(permissions: Set<Permission>, channel: Channel): Boolean = permissions.isEmpty()
}

@Serializable
data class RevoltMemberBody(
    @SerialName("_id")
    val id: RevoltMemberIdBody,
    val nickname: String? = null,
    val avatarBody: RevoltAvatarBody? = null,
) {
    fun convert(
        manager: RevoltManager,
        user: User,
        guild: Guild? = null,
    ): RevoltMember =
        RevoltMember(
            manager = manager,
            user = user,
            effectiveName = nickname ?: user.effectiveName,
            effectiveAvatarUrl = avatarBody?.getUrl(manager) ?: user.effectiveAvatarUrl,
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
