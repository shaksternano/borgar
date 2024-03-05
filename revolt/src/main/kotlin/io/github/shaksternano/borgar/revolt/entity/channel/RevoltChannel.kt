package io.github.shaksternano.borgar.revolt.entity.channel

import io.github.shaksternano.borgar.chat.entity.BaseEntity
import io.github.shaksternano.borgar.chat.entity.channel.Channel
import io.github.shaksternano.borgar.revolt.RevoltManager
import io.github.shaksternano.borgar.revolt.entity.RevoltGuild
import io.github.shaksternano.borgar.revolt.entity.RevoltUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

open class RevoltChannel(
    override val manager: RevoltManager,
    final override val id: String,
    final override val name: String,
    private val guildId: String?,
) : BaseEntity(), Channel {

    companion object {
        fun create(
            manager: RevoltManager,
            id: String,
            name: String,
            guildId: String?,
            type: RevoltChannelType,
        ) = if (type.isMessage()) {
            RevoltMessageChannel(manager, id, name, guildId)
        } else {
            RevoltChannel(manager, id, name, guildId)
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
}

@Serializable
data class RevoltChannelBody(
    @SerialName("_id")
    val id: String,
    val name: String? = null,
    @SerialName("channel_type")
    val type: String,
    @SerialName("user")
    val userId: String? = null,
    @SerialName("server")
    val guildId: String? = null,
) {
    fun convert(manager: RevoltManager, user: RevoltUser? = null): RevoltChannel =
        RevoltChannel.create(
            manager = manager,
            id = id,
            name = name ?: user?.name ?: "",
            guildId = guildId,
            type = RevoltChannelType.fromApiName(type),
        )
}
