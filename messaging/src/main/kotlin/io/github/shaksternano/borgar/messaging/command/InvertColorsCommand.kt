package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.InvertColorsTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

object InvertColorsCommand : FileCommand() {

    override val name: String = "invert"
    override val description: String = "Inverts the colors of images."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        InvertColorsTask(maxFileSize)
}
