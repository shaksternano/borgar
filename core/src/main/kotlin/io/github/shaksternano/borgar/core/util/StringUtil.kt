package io.github.shaksternano.borgar.core.util

import net.dv8tion.jda.api.utils.SplitUtil

private val URL_REGEX = "\\b((?:https?|ftp|file)://[-a-zA-Z\\d+&@#/%?=~_|!:, .;]*[-a-zA-Z\\d+&@#/%=~_|])".toRegex()
private val WHITE_SPACE_REGEX = "\\s+".toRegex()

fun CharSequence.getUrls(): List<String> {
    return URL_REGEX.findAll(this).map { it.value }.toList()
}

fun CharSequence.indexesOfPrefix(prefix: String, ignoreCase: Boolean = false): List<Int> {
    return indexesOf(prefix, ignoreCase)
        .filter {
            val isStart = it == 0 || this[it - 1].isWhitespace()
            val prefixEndIndex = it + prefix.length
            val endsWithWord = prefixEndIndex < length && !this[prefixEndIndex].isWhitespace()
            isStart && endsWithWord
        }
}

fun CharSequence.indexesOf(substr: String, ignoreCase: Boolean = false): List<Int> {
    val regex = if (ignoreCase) Regex(substr, RegexOption.IGNORE_CASE) else Regex(substr)
    return regex.findAll(this).map { it.range.first }.toList()
}

fun CharSequence.split(indexes: Iterable<Int>): List<String> {
    return indexes.windowed(2, partialWindows = true) {
        if (it.size == 2) {
            substring(it[0], it[1])
        } else {
            substring(it[0])
        }
    }
}

fun CharSequence.endOfWord(startIndex: Int): Int {
    var endIndex = startIndex
    while (endIndex < length && !this[endIndex].isWhitespace()) {
        endIndex++
    }
    return endIndex
}

fun CharSequence.splitWords(): List<String> =
    split(WHITE_SPACE_REGEX)

fun String.splitChunks(limit: Int): List<String> = SplitUtil.split(
    this,
    limit,
    true,
    SplitUtil.Strategy.NEWLINE,
    SplitUtil.Strategy.WHITESPACE,
    SplitUtil.Strategy.ANYWHERE
)
