package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.UrlFileTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

class UrlFileCommand(
    override val name: String,
    override val description: String,
    url: String,
) : FileCommand(
    inputRequirement = InputRequirement.NONE,
) {

    companion object {
        val TULIN: Command = UrlFileCommand(
            name = "tulin",
            description = "The best character in The Legend of Zelda: Breath of the Wild.",
            url = "https://cdn.revoltusercontent.com/attachments/i5fZQ7mIeaBauXxtf8Vh-bM3nWSXlZ07XSVJd7cdCN/Tulin.gif",
        )
    }

    override val deferReply: Boolean = false
    private val task: FileTask = UrlFileTask(url)

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        task
}
