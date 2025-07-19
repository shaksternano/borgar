package io.github.shaksternano.borgar.revolt.entity.channel

import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.messaging.entity.BaseEntity
import io.github.shaksternano.borgar.messaging.entity.channel.Channel
import io.github.shaksternano.borgar.revolt.RevoltManager
import io.github.shaksternano.borgar.revolt.entity.*
import io.github.shaksternano.borgar.revolt.util.RevoltPermissionValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

open class RevoltChannel(
    override val manager: RevoltManager,
    final override val id: String,
    final override val name: String,
    override val environment: ChannelEnvironment,
    val type: RevoltChannelType,
    private val guildId: String?,
    private val group: RevoltGroup?,
    val defaultPermissions: RevoltPermissionValue?,
    val rolePermissions: Map<String, RevoltPermissionValue>,
) : Channel, BaseEntity() {

    companion object {
        fun create(
            manager: RevoltManager,
            id: String,
            name: String,
            type: RevoltChannelType,
            guildId: String?,
            group: RevoltGroup?,
            defaultPermissions: RevoltPermissionValue?,
            rolePermissions: Map<String, RevoltPermissionValue>,
        ): RevoltChannel {
            val environment = when (type) {
                RevoltChannelType.GROUP -> ChannelEnvironment.GROUP
                RevoltChannelType.DIRECT_MESSAGE -> ChannelEnvironment.DIRECT_MESSAGE
                else -> ChannelEnvironment.GUILD
            }
            return if (type.isMessage()) {
                RevoltMessageChannel(
                    manager,
                    id,
                    name,
                    environment,
                    type,
                    guildId,
                    group,
                    defaultPermissions,
                    rolePermissions,
                )
            } else {
                RevoltChannel(
                    manager,
                    id,
                    name,
                    environment,
                    type,
                    guildId,
                    group,
                    defaultPermissions,
                    rolePermissions,
                )
            }
        }
    }

    override val asMention: String = "<#${id}>"
    override val asBasicMention: String = "#$name"

    private var guild: RevoltGuild? = null
    private var setGuild: Boolean = false

    override suspend fun getGuild(): RevoltGuild? {
        if (setGuild) return guild
        return guildId?.let {
            manager.getGuild(it)
        }.also {
            guild = it
            setGuild = true
        }
    }

    override suspend fun getGroup(): RevoltGroup? = group
}

@Serializable
data class RevoltChannelResponse(
    @SerialName("_id")
    val id: String,
    val name: String? = null,
    @SerialName("channel_type")
    val type: String,
    @SerialName("user")
    val userId: String? = null,
    @SerialName("server")
    val guildId: String? = null,
    @SerialName("owner")
    val ownerId: String? = null,
    @SerialName("recipients")
    val memberIds: List<String> = emptyList(),
    val icon: RevoltIconBody? = null,
    @SerialName("default_permissions")
    val defaultPermissions: RevoltRolePermissionsBody? = null,
    @SerialName("role_permissions")
    val rolePermissions: Map<String, RevoltRolePermissionsBody> = emptyMap(),
) {

    fun convert(manager: RevoltManager, user: RevoltUser? = null): RevoltChannel =
        RevoltChannel.create(
            manager = manager,
            id = id,
            name = name ?: user?.name ?: "",
            type = RevoltChannelType.fromApiName(type),
            guildId = guildId,
            group = if (type == RevoltChannelType.GROUP.apiName) RevoltGroup(
                manager = manager,
                id = id,
                name = name,
                ownerId = ownerId,
                memberIds = memberIds.toSet(),
                iconUrl = icon?.getUrl(manager),
            ) else null,
            defaultPermissions = defaultPermissions?.convert(),
            rolePermissions = rolePermissions.mapValues { it.value.convert() },
        )
}
