package com.shakster.borgar.messaging

import com.shakster.borgar.core.logger
import com.shakster.borgar.messaging.command.CommandConfig
import com.shakster.borgar.messaging.command.DerpibooruCommand
import com.shakster.borgar.messaging.command.executeCommands
import com.shakster.borgar.messaging.command.sendResponses
import com.shakster.borgar.messaging.event.CommandEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val loggerHook: MessagingAppHook = MessagingAppHook(logger.delegate)

suspend fun initMessaging() {
    logger.addHook(loggerHook)
    DerpibooruCommand.loadTags()
}

suspend fun logToChannel(channelId: String, manager: BotManager) {
    if (channelId.isBlank()) {
        return
    }
    loggerHook.addChannel(channelId, manager)
}

suspend fun CommandEvent.executeAndRespond(commandConfigs: List<CommandConfig>) {
    val channel = getChannel()
    val environment = channel.environment
    ephemeralReply = commandConfigs.any { it.command.ephemeralReply }
    val (responses, executable) = coroutineScope {
        val anyDefer = commandConfigs.any { it.command.deferReply }
        if (anyDefer) launch {
            deferReply()
        }
        executeCommands(commandConfigs, environment, this@executeAndRespond)
    }
    sendResponses(responses, executable, this, channel)
}
