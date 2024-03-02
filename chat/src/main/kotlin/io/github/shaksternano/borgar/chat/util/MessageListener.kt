package io.github.shaksternano.borgar.chat.util

import io.github.shaksternano.borgar.chat.command.parseAndExecuteCommand
import io.github.shaksternano.borgar.chat.event.MessageReceiveEvent

suspend fun onMessageReceived(event: MessageReceiveEvent) {
    if (event.getAuthor() == event.manager.getSelf()) return
    parseAndExecuteCommand(event)
    sendFavouriteFile(event)
}
