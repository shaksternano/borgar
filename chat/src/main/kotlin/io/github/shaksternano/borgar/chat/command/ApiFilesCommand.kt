package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask

abstract class ApiFilesCommand(
    private val fileCount: Int,
) : FileCommand(
    CommandArgumentInfo(
        key = "tags",
        description = "The tags to search for.",
        type = CommandArgumentType.String,
        required = false,
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
    inputRequirement = InputRequirement.NotRequired,
) {

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val tags = arguments.getStringOrEmpty("tags")
        val fileCount = if (fileCount == 1)
            arguments.getRequired(
                "filecount",
                CommandArgumentType.Integer,
            )
        else fileCount
        return createApiFilesTask(tags, fileCount, maxFileSize)
    }

    protected abstract fun createApiFilesTask(tags: String, fileCount: Int, maxFileSize: Long): FileTask
}
