package io.github.shaksternano.borgar.messaging

import io.github.shaksternano.borgar.messaging.command.CommandConfig
import io.github.shaksternano.borgar.messaging.command.DerpibooruCommand
import io.github.shaksternano.borgar.messaging.command.executeCommands
import io.github.shaksternano.borgar.messaging.command.sendResponses
import io.github.shaksternano.borgar.messaging.event.CommandEvent
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
