package io.github.shaksternano.borgar.chat

import io.github.shaksternano.borgar.chat.entity.Guild

interface BotManager {

    val maxMessageContentLength: Int
    val maxFileSize: Long
    val maxFilesPerMessage: Int

    suspend fun getGuild(id: String): Guild?
}
