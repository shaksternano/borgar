package io.github.shaksternano.borgar.chat.interaction

import io.github.shaksternano.borgar.chat.command.handleError
import io.github.shaksternano.borgar.chat.event.MessageInteractionEvent
import io.github.shaksternano.borgar.core.collect.parallelForEach
import io.github.shaksternano.borgar.core.io.deleteSilently
import io.github.shaksternano.borgar.core.logger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

val MESSAGE_INTERACTION_COMMANDS: Map<String, MessageInteractionCommand> = registerCommands(
    DownloadInteractionCommand,
)

private fun registerCommands(vararg commands: MessageInteractionCommand): Map<String, MessageInteractionCommand> = buildMap {
    commands.forEach {
        if (it.name in this) throw IllegalArgumentException(
            "A message context command with the name ${it.name} already exists. Existing command: ${this[it.name]}. New command: $it"
        )
        this[it.name] = it
    }
}

suspend fun handleMessageInteraction(event: MessageInteractionEvent) {
    val commandName = event.name
    val command = MESSAGE_INTERACTION_COMMANDS[commandName]
    if (command == null) {
        logger.error("No message context command with the name ${event.name} found")
        return
    }
    runCatching {
        event.ephemeralReply = command.ephemeral
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
}
