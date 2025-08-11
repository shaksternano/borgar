@file:JvmName("EmojiShortcodesFileGenerator")

package com.shakster.borgar.scripts.emoji

import com.shakster.borgar.core.emoji.EMOJI_FILES_DIRECTORY
import com.shakster.borgar.core.io.get
import com.shakster.borgar.core.io.useHttpClient
import com.shakster.borgar.core.util.prettyPrintJson
import io.ktor.client.statement.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.*
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

private val emojiLogger: Logger = LoggerFactory.getLogger("Emoji Shortcodes File Generator")
private const val EMOJI_SHORTCODES_FILE_NAME: String = "emojis.json"

suspend fun main() {
    val startTime = TimeSource.Monotonic.markNow()
    emojiLogger.info("Starting!")
    val directory = Path(EMOJI_FILES_DIRECTORY)
    directory.createDirectories()
    if (directory.isDirectory()) {
        val emojiShortcodesFile = directory.resolve(EMOJI_SHORTCODES_FILE_NAME)
        runCatching {
            emojiShortcodesFile.createFile()
        }
        runCatching {
            val emojiJsonString = useHttpClient { client ->
                val response =
                    client.get("https://raw.githubusercontent.com/ArkinSolomon/discord-emoji-converter/master/emojis.json")
                response.bodyAsText()
            }
            val prettyPrintedJson = prettyPrintJson(emojiJsonString)
            emojiShortcodesFile.writeText(prettyPrintedJson + "\n")
            val time = TimeSource.Monotonic.markNow()
            val timeTaken = time - startTime
            val timeTakenString = timeTaken.toString(DurationUnit.SECONDS, 3)
            emojiLogger.info("Created emoji shortcodes file $emojiShortcodesFile in $timeTakenString")
        }.onFailure {
            emojiLogger.error("Error downloading emoji shortcodes file", it)
        }
    } else if (directory.isRegularFile()) {
        emojiLogger.error("Failed to create emoji shortcodes file! The directory path $directory already exists as a file")
    } else {
        emojiLogger.error("Failed to create emoji shortcodes file! Could not create parent directory $directory")
    }
}
