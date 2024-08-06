package io.github.shaksternano.borgar.discord.entity

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.entity.BaseEntity
import io.github.shaksternano.borgar.messaging.entity.Group
import net.dv8tion.jda.api.entities.channel.concrete.GroupChannel

data class DiscordGroup(
    private val discordGroupChannel: GroupChannel,
) : Group, BaseEntity() {

    override val id: String = discordGroupChannel.id
    override val manager: BotManager = DiscordManager[discordGroupChannel.jda]
    override val name: String? = discordGroupChannel.name.ifBlank { null }
    override val ownerId: String = discordGroupChannel.ownerId.id
    override val iconUrl: String? = discordGroupChannel.iconUrl

    override suspend fun isMember(userId: String): Boolean =
        userId == discordGroupChannel.retrieveOwner().await().id
}
