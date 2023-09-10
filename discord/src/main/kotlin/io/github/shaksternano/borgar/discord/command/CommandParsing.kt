package io.github.shaksternano.borgar.discord.command

fun parseRawCommands(message: String): List<RawCommandExecutable> {
    val trimmed = message.trim()
    if (!trimmed.startsWith(COMMAND_PREFIX)) return emptyList()
    return parseCommandStrings(trimmed).map { commandString ->
        val commandEndIndex = commandString.endOfWord(COMMAND_PREFIX.length)
        val command = commandString.substring(COMMAND_PREFIX.length, commandEndIndex)
        val namedArgumentPrefixIndexes = commandString.indexesOfPrefix(NAMED_ARGUMENT_PREFIX)
        val namedArguments = commandString.split(namedArgumentPrefixIndexes)
            .associate {
                val argumentNameEndIndex = it.endOfWord(NAMED_ARGUMENT_PREFIX.length)
                val argumentName = it.substring(NAMED_ARGUMENT_PREFIX.length, argumentNameEndIndex)
                val argumentValue = it.substring(argumentNameEndIndex).trim()
                argumentName to argumentValue
            }
        val arguments = if (namedArgumentPrefixIndexes.isEmpty()) {
            commandString.substring(commandEndIndex)
        } else {
            commandString.substring(commandEndIndex, namedArgumentPrefixIndexes[0])
        }.trim()
        RawCommandExecutable(command, arguments, namedArguments)
    }
}

internal fun parseCommandStrings(message: String): List<String> {
    val commandPrefixIndexes = message.indexesOfPrefix(COMMAND_PREFIX)
    return message.split(commandPrefixIndexes).map { it.trim() }
}

private fun CharSequence.indexesOfPrefix(prefix: String, ignoreCase: Boolean = false): List<Int> {
    return indexesOf(prefix, ignoreCase)
        .filter {
            val isStart = it == 0 || this[it - 1].isWhitespace()
            val prefixEndIndex = it + prefix.length
            val endsWithWord = prefixEndIndex < length && !this[prefixEndIndex].isWhitespace()
            isStart && endsWithWord
        }
}

private fun CharSequence.indexesOf(substr: String, ignoreCase: Boolean = false): List<Int> {
    val regex = if (ignoreCase) Regex(substr, RegexOption.IGNORE_CASE) else Regex(substr)
    return regex.findAll(this).map { it.range.first }.toList()
}

private fun CharSequence.split(indexes: Iterable<Int>): List<String> {
    return indexes.windowed(2, partialWindows = true) {
        if (it.size == 2) {
            substring(it[0], it[1])
        } else {
            substring(it[0])
        }
    }
}

private fun CharSequence.endOfWord(startIndex: Int): Int {
    var endIndex = startIndex
    while (endIndex < length && !this[endIndex].isWhitespace()) {
        endIndex++
    }
    return endIndex
}

class RawCommandExecutable(
    val command: String,
    val arguments: String,
    val namedArguments: Map<String, String>,
)
