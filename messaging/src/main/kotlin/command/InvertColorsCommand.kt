package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.InvertColorsTask
import com.shakster.borgar.messaging.event.CommandEvent

object InvertColorsCommand : FileCommand() {

    override val name: String = "invert"
    override val description: String = "Inverts the colors of images."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        InvertColorsTask(maxFileSize)
}
