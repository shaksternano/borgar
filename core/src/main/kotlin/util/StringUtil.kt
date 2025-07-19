package io.github.shaksternano.borgar.core.util

import net.dv8tion.jda.api.utils.SplitUtil
import java.net.URLEncoder

val VOWELS: Set<Char> = setOf('a', 'e', 'i', 'o', 'u')

val URL_REGEX: Regex =
    "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)".toRegex()
val WHITESPACE_REGEX: Regex = "\\s+".toRegex()
val HORIZONTAL_WHITESPACE_REGEX: Regex = "\\h+".toRegex()
val NEWLINE_REGEX: Regex = "\\R".toRegex()
val NON_WHITESPACE_REGEX: Regex = "\\S+".toRegex()

fun CharSequence.startsWithVowel(): Boolean =
    isNotEmpty() && first().lowercaseChar() in VOWELS

fun CharSequence.getUrls(): List<String> =
    URL_REGEX.findAll(this).map { it.value }.toList()

fun CharSequence.indicesOfPrefix(
    prefix: String,
    ignoreCase: Boolean = false
): List<Int> = indicesOf(prefix, ignoreCase)
    .filter {
        val isStart = it == 0 || this[it - 1].isWhitespace()
        val prefixEndIndex = it + prefix.length
        val endsWithWord = prefixEndIndex < length && !this[prefixEndIndex].isWhitespace()
        isStart && endsWithWord
    }

fun CharSequence.indicesOf(substr: String, ignoreCase: Boolean = false): List<Int> {
    val regex =
        if (ignoreCase) Regex(substr, RegexOption.IGNORE_CASE)
        else Regex(substr)
    return regex.findAll(this).map { it.range.first }.toList()
}

fun CharSequence.split(indices: Iterable<Int>): List<String> =
    indices.windowed(2, partialWindows = true) {
        if (it.size == 2) {
            substring(it[0], it[1])
        } else {
            substring(it[0])
        }
    }

fun CharSequence.endOfWord(startIndex: Int): Int {
    var endIndex = startIndex
    while (endIndex < length && !this[endIndex].isWhitespace()) {
        endIndex++
    }
    return endIndex
}

fun CharSequence.splitWords(limit: Int = 0): List<String> =
    split(WHITESPACE_REGEX, limit).filter { it.isNotBlank() }

fun String.splitChunks(limit: Int): List<String> = SplitUtil.split(
    this,
    limit,
    true,
    SplitUtil.Strategy.NEWLINE,
    SplitUtil.Strategy.WHITESPACE,
    SplitUtil.Strategy.ANYWHERE,
)

fun String.equalsAnyIgnoreCase(vararg toCompare: String): Boolean = toCompare.any {
    equals(it, ignoreCase = true)
}

fun CharSequence.splitCamelCase(): String =
    replace(
        String.format(
            "%s|%s|%s",
            "(?<=[A-Z])(?=[A-Z][a-z])",
            "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])",
        ).toRegex(),
        " ",
    )

fun parseTags(tags: String): Set<String> =
    tags
        .split(',')
        .map {
            it.trim()
        }
        .filter {
            it.isNotEmpty()
        }
        .toSet()

fun String.encodeUrl(): String {
    val spaceIndices = indicesOf(" ")
    val encoded = URLEncoder.encode(this, Charsets.UTF_8)
    return spaceIndices.asReversed().fold(StringBuilder(encoded)) { result, index ->
        result.replace(index, index + 1, "%20")
    }.toString()
}
