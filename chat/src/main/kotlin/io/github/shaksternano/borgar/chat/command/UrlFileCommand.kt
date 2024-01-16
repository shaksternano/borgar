package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask

class UrlFileCommand(
    override val name: String,
    override val description: String,
    private val url: String,
) : FileCommand(
    requireInput = false,
) {

    companion object {
        val HAEMA = UrlFileCommand(
            "haema",
            "https://www.curseforge.com/minecraft/mc-mods/haema",
            "https://media.discordapp.net/attachments/964551969509347331/1134818935829712987/YOU_SHOULD_DOWNLOAD_HAEMA_NOW.gif",
        )

        val TULIN = UrlFileCommand(
            "tulin",
            "The best character in The Legend of Zelda: Breath of the Wild.",
            "https://media.discordapp.net/attachments/964551969509347331/1119756264633798729/BotW_Tulin_Model.gif",
        )
    }

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        UrlFileTask(url)
}
