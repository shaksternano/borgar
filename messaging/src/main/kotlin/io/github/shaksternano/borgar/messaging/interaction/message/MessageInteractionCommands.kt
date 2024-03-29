package io.github.shaksternano.borgar.messaging.interaction.message

import io.github.shaksternano.borgar.core.collect.parallelForEach
import io.github.shaksternano.borgar.core.io.deleteSilently
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.messaging.command.handleError
import io.github.shaksternano.borgar.messaging.command.registerCommands
import io.github.shaksternano.borgar.messaging.event.InteractionEvent
import io.github.shaksternano.borgar.messaging.event.MessageInteractionEvent
import io.github.shaksternano.borgar.messaging.interaction.InteractionCommand
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

val MESSAGE_INTERACTION_COMMANDS: Map<String, MessageInteractionCommand> = registerCommands(
    "message interaction command",
    DownloadInteractionCommand,
)

suspend fun handleMessageInteraction(event: MessageInteractionEvent) {
    val commandName = event.name
    val command = MESSAGE_INTERACTION_COMMANDS[commandName]
    if (command == null) {
        logger.error("No message interaction command with the name ${event.name} found")
        return
    }
    handleInteractionCommand(command, event)
}

suspend fun <T : InteractionEvent> handleInteractionCommand(command: InteractionCommand<T>, event: T) =
    runCatching {
        event.ephemeralReply = command.ephemeralReply
        val response = coroutineScope {
            if (command.deferReply) launch {
                event.deferReply()
            }
            command.respond(event)
        }
        try {
            val sent = event.reply(response)
            command.onResponseSend(response, sent, event)
        } finally {
            response.files.parallelForEach {
                it.path?.deleteSilently()
            }
        }
    }.onFailure {
        val responseContent = handleError(it, event.manager)
        event.reply(responseContent)
    }
