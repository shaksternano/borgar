package com.shakster.borgar.stoat.entity.channel

import com.shakster.borgar.core.util.ChannelEnvironment
import com.shakster.borgar.messaging.entity.BaseEntity
import com.shakster.borgar.messaging.entity.channel.Channel
import com.shakster.borgar.stoat.StoatManager
import com.shakster.borgar.stoat.entity.*
import com.shakster.borgar.stoat.util.StoatPermissionValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

open class StoatChannel(
    override val manager: StoatManager,
    final override val id: String,
    final override val name: String,
    override val environment: ChannelEnvironment,
    val type: StoatChannelType,
    private val guildId: String?,
    private val group: StoatGroup?,
    val defaultPermissions: StoatPermissionValue?,
    val rolePermissions: Map<String, StoatPermissionValue>,
) : Channel, BaseEntity() {

    companion object {
        fun create(
            manager: StoatManager,
            id: String,
            name: String,
            type: StoatChannelType,
            guildId: String?,
            group: StoatGroup?,
            defaultPermissions: StoatPermissionValue?,
            rolePermissions: Map<String, StoatPermissionValue>,
        ): StoatChannel {
            val environment = when (type) {
                StoatChannelType.GROUP -> ChannelEnvironment.GROUP
                StoatChannelType.DIRECT_MESSAGE -> ChannelEnvironment.DIRECT_MESSAGE
                else -> ChannelEnvironment.GUILD
            }
            return if (type.isMessage()) {
                StoatMessageChannel(
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
                StoatChannel(
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

    private var guild: StoatGuild? = null
    private var setGuild: Boolean = false

    override suspend fun getGuild(): StoatGuild? {
        if (setGuild) return guild
        return guildId?.let {
            manager.getGuild(it)
        }.also {
            guild = it
            setGuild = true
        }
    }

    override suspend fun getGroup(): StoatGroup? = group
}

@Serializable
data class StoatChannelResponse(
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
    val icon: StoatIconBody? = null,
    @SerialName("default_permissions")
    val defaultPermissions: StoatRolePermissionsBody? = null,
    @SerialName("role_permissions")
    val rolePermissions: Map<String, StoatRolePermissionsBody> = emptyMap(),
) {

    fun convert(manager: StoatManager, user: StoatUser? = null): StoatChannel =
        StoatChannel.create(
            manager = manager,
            id = id,
            name = name ?: user?.name ?: "",
            type = StoatChannelType.fromApiName(type),
            guildId = guildId,
            group = if (type == StoatChannelType.GROUP.apiName) StoatGroup(
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
