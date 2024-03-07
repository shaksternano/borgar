package io.github.shaksternano.borgar.chat.entity.channel

import io.github.shaksternano.borgar.chat.entity.Guild
import io.github.shaksternano.borgar.chat.entity.Mentionable

interface Channel : Mentionable {

    val name: String

    suspend fun getGuild(): Guild?

    suspend fun getMaxFileSize(): Long {
        return getGuild()?.maxFileSize ?: manager.maxFileSize
    }
}
