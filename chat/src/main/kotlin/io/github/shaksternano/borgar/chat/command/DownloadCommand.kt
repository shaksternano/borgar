package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.DownloadTask
import io.github.shaksternano.borgar.core.io.task.FileTask

object DownloadCommand : FileCommand(
    CommandArgumentInfo(
        key = "url",
        description = "The URL to download from.",
        type = CommandArgumentType.String,
        required = true,
    ),
    CommandArgumentInfo(
        key = "audioonly",
        aliases = setOf("a"),
        description = "Whether to only download audio or not. Not all websites support this.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
    CommandArgumentInfo(
        key = "filenumber",
        aliases = setOf("n"),
        description = "The file to download. If not specified, all files will be downloaded.",
        type = CommandArgumentType.Integer,
        required = false,
        validator = PositiveIntValidator,
    ),
    inputRequirement = InputRequirement.NotRequired,
) {

    override val name: String = "download"
    override val aliases: Set<String> = setOf("dl")
    override val description: String =
        "Downloads a file from a social media website, for example, a video from YouTube."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val audioOnly = arguments.getRequired("audioonly", CommandArgumentType.Boolean)
        val fileNumber = arguments.getOptional("filenumber", CommandArgumentType.Integer)
        return DownloadTask(audioOnly, fileNumber, maxFileSize)
    }
}
