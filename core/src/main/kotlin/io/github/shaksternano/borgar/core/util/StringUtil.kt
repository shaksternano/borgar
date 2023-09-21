package io.github.shaksternano.borgar.core.util

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
