package io.github.shaksternano.borgar.scripts.emoji

import io.github.shaksternano.borgar.core.emoji.EMOJI_FILES_DIRECTORY
import io.github.shaksternano.borgar.core.io.filenameWithoutExtension
import io.github.shaksternano.borgar.scripts.util.getGitHubLatestReleaseTagCommitSha
import io.github.shaksternano.borgar.scripts.util.listGitHubFiles
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.*
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

private val emojiLogger: Logger = LoggerFactory.getLogger("Emoji Unicodes File Generator")

private const val REPOSITORY_OWNER: String = "twitter"
private const val REPOSITORY_NAME: String = "twemoji"

private const val EMOJI_UNICODES_FILE_NAME: String = "emoji_unicodes.txt"

/**
 * Generates a file containing the unicodes of all the characters that have a
 * corresponding [Twemoji](https://github.com/twitter/twemoji) image.
 * The relative path of the generated file will be `src/main/resources/emoji/emoji_unicodes.txt`.
 */
suspend fun main() {
    val startTime = TimeSource.Monotonic.markNow()
    emojiLogger.info("Starting!")
    val directory = Path(EMOJI_FILES_DIRECTORY)
    directory.createDirectories()
    if (directory.isDirectory()) {
        val sha = getGitHubLatestReleaseTagCommitSha(REPOSITORY_OWNER, REPOSITORY_NAME)
        val filenames = listGitHubFiles(REPOSITORY_OWNER, REPOSITORY_NAME, sha, "assets", "72x72")
        if (filenames.isEmpty()) {
            emojiLogger.error("Failed to load emoji unicodes, could not retrieve any file names!")
        } else {
            val emojiUnicodes = filenames.map {
                filenameWithoutExtension(it).lowercase()
            }
            val emojiUnicodesPath = directory.resolve(EMOJI_UNICODES_FILE_NAME)
            runCatching {
                emojiUnicodesPath.writeLines(emojiUnicodes)
                val time = TimeSource.Monotonic.markNow()
                val timeTaken = time - startTime
                val timeTakenString = timeTaken.toString(DurationUnit.SECONDS, 3)
                emojiLogger.info("Created emoji unicodes file $emojiUnicodesPath in $timeTakenString")
            }.onFailure {
                emojiLogger.error("Failed to create emoji unicodes file under $emojiUnicodesPath", it)
            }
        }
    } else if (directory.isRegularFile()) {
        emojiLogger.error("Failed to create emoji unicodes file! The directory path \"$directory\" already exists as a file!")
    } else {
        emojiLogger.error("Failed to create emoji unicodes file! Could not create parent directory \"$directory\"!")
    }
}
