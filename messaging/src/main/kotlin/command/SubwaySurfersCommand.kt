package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.SubwaySurfersTask
import com.shakster.borgar.messaging.event.CommandEvent

object SubwaySurfersCommand : FileCommand() {

    override val name: String = "subwaysurfers"
    override val aliases: Set<String> = setOf("subway")
    override val description: String = "Adds Subway Surfers gameplay to media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        SubwaySurfersTask(maxFileSize)
}
