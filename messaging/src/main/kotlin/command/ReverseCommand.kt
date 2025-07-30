package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.SpeedTask
import com.shakster.borgar.messaging.event.CommandEvent

object ReverseCommand : FileCommand() {

    override val name: String = "reverse"
    override val description: String = "Reverses animated media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        SpeedTask(
            speed = -1.0,
            maxFileSize = maxFileSize,
        )
}
