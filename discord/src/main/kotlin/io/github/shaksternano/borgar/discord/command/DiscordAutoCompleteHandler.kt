package io.github.shaksternano.borgar.discord.command

import dev.minn.jda.ktx.coroutines.await
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.discord.DiscordManager
import io.github.shaksternano.borgar.messaging.command.COMMANDS
import io.github.shaksternano.borgar.messaging.command.CommandAutoCompleteHandler
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

fun registerAutoCompleteHandlers() {
    COMMANDS.values
        .flatMap { command ->
            command.argumentInfo.associateBy { command.name to it.key }.entries
        }
        .mapNotNull { (key, argumentInfo) ->
            argumentInfo.autoCompleteHandler?.let { handler ->
                Triple(key.first, key.second, handler)
            }
        }
        .forEach { (command, option, handler) ->
            registerAutoCompleteHandler(command, option, handler)
        }
}

suspend fun handleCommandAutoComplete(event: CommandAutoCompleteInteractionEvent) {
    val command = event.name
    val argument = event.focusedOption.name
    val handler = getAutoCompleteHandler(command, argument) ?: return
    val currentValue = event.focusedOption.value
    val manager = DiscordManager[event.jda]
    when (handler) {
        is CommandAutoCompleteHandler.Long -> {
            val longValue = currentValue.toLongOrNull()
            if (longValue == null) {
                logger.error("Invalid long value: $currentValue")
                return
            }
            val values = handler.handleAutoComplete(
                command,
                argument,
                longValue,
                manager,
            )
            event.replyChoiceLongs(values).await()
        }

        is CommandAutoCompleteHandler.Double -> {
            val doubleValue = currentValue.toDoubleOrNull()
            if (doubleValue == null) {
                logger.error("Invalid double value: $currentValue")
                return
            }
            val values = handler.handleAutoComplete(
                command,
                argument,
                doubleValue,
                manager,
            )
            event.replyChoiceDoubles(values).await()
        }

        is CommandAutoCompleteHandler.String -> {
            val values = handler.handleAutoComplete(
                command,
                argument,
                currentValue,
                manager,
            )
            event.replyChoiceStrings(values).await()
        }
    }
}


private data class AutoCompleteHandlerKey(
    val command: String,
    val argument: String,
)

private val autoCompleteHandlers: MutableMap<AutoCompleteHandlerKey, CommandAutoCompleteHandler<*>> = mutableMapOf()

private fun registerAutoCompleteHandler(command: String, argument: String, handler: CommandAutoCompleteHandler<*>) {
    autoCompleteHandlers[AutoCompleteHandlerKey(command, argument)] = handler
}

private fun getAutoCompleteHandler(command: String, argument: String): CommandAutoCompleteHandler<*>? {
    return autoCompleteHandlers[AutoCompleteHandlerKey(command, argument)]
}
