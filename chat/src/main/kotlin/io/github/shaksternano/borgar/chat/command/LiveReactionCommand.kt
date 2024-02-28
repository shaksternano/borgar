package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.LiveReactionTask

object LiveReactionCommand : FileCommand() {

    override val name: String = "livereaction"
    override val aliases: Set<String> = setOf("live")
    override val description: String = "Live reaction meme."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        LiveReactionTask(maxFileSize)
}
