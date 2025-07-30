package com.shakster.borgar.messaging.entity.channel

import com.shakster.borgar.core.util.ChannelEnvironment
import com.shakster.borgar.messaging.entity.Group
import com.shakster.borgar.messaging.entity.Guild
import com.shakster.borgar.messaging.entity.Mentionable

interface Channel : Mentionable {

    override val name: String
    val environment: ChannelEnvironment

    suspend fun getGuild(): Guild?

    suspend fun getGroup(): Group?

    suspend fun getMaxFileSize(): Long {
        return getGuild()?.maxFileSize ?: manager.maxFileSize
    }
}
