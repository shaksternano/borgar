package com.shakster.borgar.messaging

import com.shakster.borgar.messaging.command.CommandConfig
import com.shakster.borgar.messaging.command.DerpibooruCommand
import com.shakster.borgar.messaging.command.executeCommands
import com.shakster.borgar.messaging.command.sendResponses
import com.shakster.borgar.messaging.event.CommandEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun initMessaging() {
    DerpibooruCommand.loadTags()
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
