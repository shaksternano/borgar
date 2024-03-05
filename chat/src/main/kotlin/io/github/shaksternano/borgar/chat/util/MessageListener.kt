package io.github.shaksternano.borgar.chat.util

import io.github.shaksternano.borgar.chat.command.parseAndExecuteCommand
import io.github.shaksternano.borgar.chat.event.MessageReceiveEvent
import io.github.shaksternano.borgar.core.logger

suspend fun onMessageReceived(event: MessageReceiveEvent) {
    runCatching {
        if (event.getAuthor() == event.manager.getSelf()) return
        parseAndExecuteCommand(event)
        sendFavouriteFile(event)
    }.onFailure {
        logger.error("Error handling message event", it)
    }
}
