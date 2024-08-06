package io.github.shaksternano.borgar.discord.command

import io.github.shaksternano.borgar.messaging.command.CommandAutoCompleteHandler

private data class AutoCompleteHandlerKey(
    val command: String,
    val argument: String,
)

private val autoCompleteHandlers: MutableMap<AutoCompleteHandlerKey, CommandAutoCompleteHandler<*>> = mutableMapOf()

fun registerAutoCompleteHandler(command: String, argument: String, handler: CommandAutoCompleteHandler<*>) {
    autoCompleteHandlers[AutoCompleteHandlerKey(command, argument)] = handler
}

fun getAutoCompleteHandler(command: String, argument: String): CommandAutoCompleteHandler<*>? {
    return autoCompleteHandlers[AutoCompleteHandlerKey(command, argument)]
}
