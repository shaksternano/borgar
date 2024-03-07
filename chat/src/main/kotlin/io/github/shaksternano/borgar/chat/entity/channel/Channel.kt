package io.github.shaksternano.borgar.chat.entity.channel

import io.github.shaksternano.borgar.chat.entity.Group
import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Mentionable
import io.github.shaksternano.borgar.core.util.ChannelEnvironment

interface Channel : Mentionable {

    val name: String
    val environment: ChannelEnvironment

    suspend fun getGuild(): Guild?

    suspend fun getGroup(): Group?

    suspend fun getMaxFileSize(): Long {
        return getGuild()?.maxFileSize ?: manager.maxFileSize
    }
}
