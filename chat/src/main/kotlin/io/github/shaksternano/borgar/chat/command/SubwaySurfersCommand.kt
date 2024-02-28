package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.SubwaySurfersTask

object SubwaySurfersCommand : FileCommand() {

    override val name: String = "subwaysurfers"
    override val aliases: Set<String> = setOf("subway")
    override val description: String = "Adds Subway Surfers gameplay to media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        SubwaySurfersTask(maxFileSize)
}
