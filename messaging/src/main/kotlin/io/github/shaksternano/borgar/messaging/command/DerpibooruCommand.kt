package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.BotConfig
import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.task.DerpibooruTask
import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.util.WHITESPACE_REGEX
import io.github.shaksternano.borgar.messaging.BotManager
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import io.ktor.client.statement.*
import kotlin.io.path.*

class DerpibooruCommand(
    override val name: String,
    override val description: String,
    private val fileCount: Int,
) : FileCommand(
    CommandArgumentInfo(
        key = "tags",
        description = "The tags to search for.",
        type = CommandArgumentType.String,
        required = false,
        defaultValue = "safe",
        autoCompleteHandler = TagAutoCompleteHandler,
    ),
    CommandArgumentInfo(
        key = "searchall",
        aliases = setOf("all", "a"),
        description = "Whether to search for NSFW images or not.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
    *if (fileCount == 1) {
        arrayOf(
            CommandArgumentInfo(
                key = "id",
                description = "The ID of the image to send.",
                type = CommandArgumentType.String,
                required = false,
            ),
            CommandArgumentInfo(
                key = "filecount",
                aliases = setOf("n"),
                description = "The number of images to send.",
                type = CommandArgumentType.Integer,
                required = false,
                defaultValue = 1,
                validator = RangeValidator(1..10),
            ),
        )
    } else {
        emptyArray()
    },
    inputRequirement = InputRequirement.NONE,
) {

    companion object {
        val DERPIBOORU: Command = DerpibooruCommand(
            name = "pony",
            description = "Sends a random pony image.",
            fileCount = 1,
        )

        val DERPIBOORU_BOMB: Command = DerpibooruCommand(
            name = "ponybomb",
            description = "Sends a bunch of random pony images.",
            fileCount = 10,
        )

        private var tags: List<String> = listOf()

        suspend fun loadTags() {
            val tagsFile = Path("derpibooru_tags.txt")
            tags = if (tagsFile.isRegularFile()) {
                tagsFile.readLines()
            } else {
                val tagsUrl = BotConfig.get().derpibooru.tagsUrl.ifBlank { return }
                val filteredTagsUrl = BotConfig.get().derpibooru.filteredTagsUrl
                val filteredTags = if (filteredTagsUrl.isBlank()) {
                    listOf()
                } else {
                    useHttpClient { client ->
                        val response = client.get(filteredTagsUrl)
                        response.bodyAsText()
                            .lines()
                            .filter { it.isNotBlank() }
                            .distinct()
                    }
                }
                val tempFile = createTemporaryFile("derpibooru_tags.csv")
                download(tagsUrl, tempFile)
                val uniqueTags = mutableSetOf<String>()
                tempFile.forEachLine { line ->
                    val split = line.split(",", limit = 3)
                    val tag = split.getOrElse(0) { return@forEachLine }
                    val tagsAndAliases = mutableSetOf(tag.lowercase())
                    val aliases = split.getOrNull(2)
                    aliases?.removeSurrounding("\"")?.split(",")?.forEach {
                        if (it.isNotBlank()) {
                            tagsAndAliases.add(it.lowercase())
                        }
                    }
                    val hasFiltered = tagsAndAliases.any { tagOrAlias ->
                        filteredTags.any { filtered ->
                            tagOrAlias.contains(filtered, ignoreCase = true)
                        }
                    }
                    if (!hasFiltered) {
                        uniqueTags.addAll(tagsAndAliases)
                    }
                }
                tempFile.deleteSilently()
                tagsFile.writeLines(uniqueTags)
                uniqueTags.toList()
            }
        }
    }

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val id = arguments["id", CommandArgumentType.String]
        if (id != null) {
            return DerpibooruTask(
                tags = "",
                id = id,
                searchAll = false,
                fileCount = 1,
                maxFileSize = maxFileSize,
            )
        }
        val tags = arguments.getRequired("tags", CommandArgumentType.String)
        val searchAll = arguments.getRequired("searchall", CommandArgumentType.Boolean)
        val fileCount =
            if (fileCount == 1) arguments.getRequired("filecount", CommandArgumentType.Integer)
            else fileCount
        return DerpibooruTask(
            tags = tags,
            id = null,
            searchAll = searchAll,
            fileCount = fileCount,
            maxFileSize = maxFileSize,
        )
    }

    private data object TagAutoCompleteHandler : CommandAutoCompleteHandler.String {

        override suspend fun handleAutoComplete(
            command: String,
            argument: String,
            currentValue: String,
            manager: BotManager,
        ): List<String> {
            if (currentValue.isBlank()) {
                return emptyList()
            }
            val lastCommaPosition = currentValue.lastIndexOf(',')
            val lastTagStart = if (lastCommaPosition == -1) {
                0
            } else {
                val whiteSpaceEnd = WHITESPACE_REGEX.find(currentValue, startIndex = lastCommaPosition)
                    ?.range
                    ?.last
                    ?: lastCommaPosition
                if (whiteSpaceEnd >= currentValue.length - 1) {
                    return emptyList()
                }
                whiteSpaceEnd + 1
            }
            val lastTag = currentValue.substring(lastTagStart).trim()
            return tags.asSequence()
                .filter { it.contains(lastTag, ignoreCase = true) }
                .take(manager.commandAutoCompleteMaxSuggestions)
                .map {
                    currentValue.substring(0, lastTagStart).trim() + " " + it.trim()
                }
                .toList()
        }
    }
}
