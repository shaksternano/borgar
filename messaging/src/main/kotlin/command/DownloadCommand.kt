package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.exception.ErrorResponseException
import com.shakster.borgar.core.task.DownloadTask
import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.util.getUrls
import com.shakster.borgar.messaging.event.CommandEvent
import com.shakster.borgar.messaging.util.searchExceptSelf

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
    inputRequirement = InputRequirement.NONE,
) {

    override val name: String = "download"
    override val aliases: Set<String> = setOf("dl")
    override val description: String =
        "Downloads a file from a social media website, for example, a video from YouTube."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val url = arguments.getOptional("url", CommandArgumentType.String)
            ?: event.asMessageIntersection(arguments).searchExceptSelf {
                it.content.getUrls().firstOrNull()
            }
            ?: throw ErrorResponseException("No URL specified!")
        val audioOnly = arguments.getRequired("audioonly", CommandArgumentType.Boolean)
        val fileNumber = arguments.getOptional("filenumber", CommandArgumentType.Integer)
        return DownloadTask(url, audioOnly, fileNumber, maxFileSize)
    }
}
