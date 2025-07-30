package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.UrlFileTask
import com.shakster.borgar.messaging.event.CommandEvent

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
