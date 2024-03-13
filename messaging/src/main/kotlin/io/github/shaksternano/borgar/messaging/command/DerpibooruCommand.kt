package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.io.task.DerpibooruTask
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

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
}
