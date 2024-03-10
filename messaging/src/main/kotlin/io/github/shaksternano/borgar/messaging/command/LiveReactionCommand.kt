package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.LiveReactionTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

object LiveReactionCommand : FileCommand() {

    override val name: String = "livereaction"
    override val aliases: Set<String> = setOf("live")
    override val description: String = "Live reaction meme."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        LiveReactionTask(maxFileSize)
}
