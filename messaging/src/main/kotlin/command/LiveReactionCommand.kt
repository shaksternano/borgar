package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.LiveReactionTask
import com.shakster.borgar.messaging.event.CommandEvent

object LiveReactionCommand : FileCommand() {

    override val name: String = "livereaction"
    override val aliases: Set<String> = setOf("live")
    override val description: String = "Live reaction meme."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        LiveReactionTask(maxFileSize)
}
