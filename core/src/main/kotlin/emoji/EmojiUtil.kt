package com.shakster.borgar.core.emoji

import com.shakster.borgar.core.io.IO_DISPATCHER
import com.shakster.borgar.core.io.getResource
import com.shakster.borgar.core.logger
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

const val EMOJI_FILES_DIRECTORY: String = "core/src/main/resources/emoji"

private val emojiUnicodeSet: MutableSet<String> = mutableSetOf()
private val emojiShortcodesToUrls: MutableMap<String, String> = mutableMapOf()

suspend fun initEmojis() {
    initEmojiUnicodeSet()
    initEmojiShortCodesToUrlsMap()
}

private suspend fun initEmojiUnicodeSet() {
    runCatching {
        val lines = withContext(IO_DISPATCHER) {
            BufferedReader(InputStreamReader(getResource("emoji/emoji_unicodes.txt"))).use {
                it.readLines()
            }
        }
        emojiUnicodeSet.clear()
        val lowercaseLines = lines.map { it.lowercase() }
        emojiUnicodeSet.addAll(lowercaseLines)
    }.onFailure {
        logger.error("Error while loading emoji unicodes!", it)
    }
}

private suspend fun initEmojiShortCodesToUrlsMap() {
    runCatching {
        val jsonString = withContext(IO_DISPATCHER) {
            BufferedReader(InputStreamReader(getResource("emoji/emojis.json"))).use {
                it.readText()
            }
        }
        val emojis = Json.decodeFromString<Map<String, String>>(jsonString)
        val emojiUrls = emojis.mapValues { (_, emoji) ->
            val emojiUnicode = emoji.codePoints()
                .mapToObj(Integer::toHexString)
                .collect(Collectors.joining("-"))
            getEmojiUrl(emojiUnicode)
        }
        emojiShortcodesToUrls.clear()
        emojiShortcodesToUrls.putAll(emojiUrls)
    }.onFailure {
        logger.error("Error while loading emoji shortcodes", it)
    }
}

fun isEmojiUnicode(unicode: String): Boolean {
    return emojiUnicodeSet.contains(unicode.lowercase())
}

fun getEmojiUrlFromShortcode(shortcode: String): String? =
    emojiShortcodesToUrls[shortcode]

fun getEmojiUrl(unicode: String): String =
    "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/$unicode.png"
