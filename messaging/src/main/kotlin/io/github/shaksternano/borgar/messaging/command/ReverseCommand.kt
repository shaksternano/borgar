package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.SpeedTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

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
