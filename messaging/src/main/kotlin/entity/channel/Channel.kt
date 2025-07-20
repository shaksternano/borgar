package io.github.shaksternano.borgar.messaging.entity.channel

import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.messaging.entity.Group
import io.github.shaksternano.borgar.messaging.entity.Guild
import io.github.shaksternano.borgar.messaging.entity.Mentionable

interface Channel : Mentionable {

    override val name: String
    val environment: ChannelEnvironment

    suspend fun getGuild(): Guild?

    suspend fun getGroup(): Group?

    suspend fun getMaxFileSize(): Long {
        return getGuild()?.maxFileSize ?: manager.maxFileSize
    }
}
