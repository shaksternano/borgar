package com.shakster.borgar.revolt

import com.shakster.borgar.core.BotConfig
import com.shakster.borgar.core.logger
import com.shakster.borgar.messaging.exception.InvalidTokenException
import com.shakster.borgar.messaging.logToChannel
import com.shakster.borgar.messaging.registerBotManager

suspend fun initRevolt(token: String) {
    val manager = RevoltManager(token)
    try {
        manager.init()
    } catch (_: InvalidTokenException) {
        logger.error("Invalid Revolt token")
        return
    }
    registerBotManager(manager)
    logToChannel(BotConfig.get().revolt.logChannelId, manager)
    logger.info("Connected to Revolt")
}
