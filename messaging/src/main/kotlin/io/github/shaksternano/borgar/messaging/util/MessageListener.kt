package io.github.shaksternano.borgar.messaging.util

import io.github.shaksternano.borgar.core.data.repository.BanRepository
import io.github.shaksternano.borgar.core.data.repository.EntityType
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

suspend inline fun handleBanned(event: MessageReceiveEvent, type: String, ifBanned: () -> Unit) {
    if (BanRepository.exists(
            event.authorId,
            EntityType.USER,
            event.manager.platform,
        )
    ) {
        val message = "Ignoring $type from banned user \"${event.getAuthor().name}\" (${event.authorId})" +
            " sent in channel \"${event.getChannel().name}\" (${event.channelId})" +
            " on ${event.manager.platform.displayName}"
        logger.info(message)
        ifBanned()
    }
}
