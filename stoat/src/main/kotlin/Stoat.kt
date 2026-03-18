package com.shakster.borgar.stoat

import com.shakster.borgar.core.BotConfig
import com.shakster.borgar.core.logger
import com.shakster.borgar.messaging.exception.InvalidTokenException
import com.shakster.borgar.messaging.logToChannel
import com.shakster.borgar.messaging.registerBotManager

suspend fun initStoat(token: String) {
    logger.info("Connecting to Stoat...")
    val manager = StoatManager(token)
    try {
        manager.init()
    } catch (_: InvalidTokenException) {
        logger.error("Invalid Stoat token")
        return
    }
    registerBotManager(manager)
    logToChannel(BotConfig.get().stoat.logChannelId, manager)
}
