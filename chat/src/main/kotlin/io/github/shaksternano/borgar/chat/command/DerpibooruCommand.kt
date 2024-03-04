package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.DerpibooruTask
import io.github.shaksternano.borgar.core.io.task.FileTask

class DerpibooruCommand(
    override val name: String,
    override val aliases: Set<String>,
    override val description: String,
    private val fileCount: Int,
) : FileCommand(
    CommandArgumentInfo(
        key = "tags",
        description = "The tags to search for.",
        type = CommandArgumentType.String,
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
    inputRequirement = InputRequirement.None,
) {

    companion object {
        val DERPIBOORU: Command = DerpibooruCommand(
            name = "derpibooru",
            aliases = setOf("derpi"),
            description = "Sends a random image from Derpibooru.",
            fileCount = 1,
        )

        val DERPIBOORU_BOMB: Command = DerpibooruCommand(
            name = "derpiboorubomb",
            aliases = setOf("derpibomb"),
            description = "Sends a bunch of random images from Derpibooru.",
            fileCount = 10,
        )
    }

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val tags = arguments.getRequired("tags", CommandArgumentType.String)
        val searchAll = arguments.getRequired("searchall", CommandArgumentType.Boolean)
        val fileCount =
            if (fileCount == 1) arguments.getRequired("filecount", CommandArgumentType.Integer)
            else fileCount
        return DerpibooruTask(tags, searchAll, fileCount, maxFileSize)
    }
}
