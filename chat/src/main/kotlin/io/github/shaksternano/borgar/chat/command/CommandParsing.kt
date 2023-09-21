package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.core.util.endOfWord
import io.github.shaksternano.borgar.core.util.indexesOfPrefix
import io.github.shaksternano.borgar.core.util.split

fun parseRawCommands(message: String): List<RawCommandConfig> {
    return parseCommandStrings(message).map { commandString ->
        val commandEndIndex = commandString.endOfWord(COMMAND_PREFIX.length)
        val command = commandString.substring(COMMAND_PREFIX.length, commandEndIndex)
        val argumentPrefixIndexes = commandString.indexesOfPrefix(ARGUMENT_PREFIX)
        val arguments = commandString.split(argumentPrefixIndexes)
            .associate {
                val argumentNameEndIndex = it.endOfWord(ARGUMENT_PREFIX.length)
                val argumentName = it.substring(ARGUMENT_PREFIX.length, argumentNameEndIndex)
                val argumentValue = it.substring(argumentNameEndIndex).trim()
                argumentName to argumentValue
            }
        val defaultArgument = if (argumentPrefixIndexes.isEmpty()) {
            commandString.substring(commandEndIndex)
        } else {
            commandString.substring(commandEndIndex, argumentPrefixIndexes[0])
        }.trim()
        RawCommandConfig(command, arguments, defaultArgument)
    }
}

internal fun parseCommandStrings(message: String): List<String> {
    val commandPrefixIndexes = message.indexesOfPrefix(COMMAND_PREFIX)
    return message.split(commandPrefixIndexes).map { it.trim() }
}

class RawCommandConfig(
    val command: String,
    val arguments: Map<String, String>,
    val defaultArgument: String,
)
