package io.github.shaksternano.borgar.messaging.util

import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.messaging.command.parseAndExecuteCommand
import io.github.shaksternano.borgar.messaging.event.MessageReceiveEvent

suspend fun onMessageReceived(event: MessageReceiveEvent) {
    runCatching {
        if (event.authorId == event.manager.selfId) return
        parseAndExecuteCommand(event)
        sendFavouriteFile(event)
    }.onFailure {
        logger.error("Error handling message event", it)
    }
}
