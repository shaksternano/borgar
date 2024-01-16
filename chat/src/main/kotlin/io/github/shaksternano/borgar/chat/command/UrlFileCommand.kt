package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask

class UrlFileCommand(
    override val name: String,
    override val description: String,
    url: String,
) : FileCommand(
    requireInput = false,
) {

    private val task: FileTask = UrlFileTask(url)

    companion object {
        val HAEMA = UrlFileCommand(
            name = "haema",
            description = "https://modrinth.com/mod/haema",
            url = "https://media.discordapp.net/attachments/964551969509347331/1134818935829712987/YOU_SHOULD_DOWNLOAD_HAEMA_NOW.gif",
        )

        val TULIN = UrlFileCommand(
            name = "tulin",
            description = "The best character in The Legend of Zelda: Breath of the Wild.",
            url = "https://media.discordapp.net/attachments/964551969509347331/1119756264633798729/BotW_Tulin_Model.gif",
        )
    }

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        task
}
