package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.SpeedTask

object ReverseCommand : FileCommand() {

    override val name: String = "reverse"
    override val description: String = "Reverses animated media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        SpeedTask(
            speed = -1.0,
            maxFileSize = maxFileSize,
            outputName = "reversed"
        )
}
