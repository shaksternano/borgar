package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.FlipTask
import com.shakster.borgar.messaging.event.CommandEvent

object FlipCommand : FileCommand(
    CommandArgumentInfo(
        key = "vertical",
        aliases = setOf("v"),
        description = "Whether to flip vertically or not.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
) {

    override val name: String = "flip"
    override val description: String = "Flips media horizontally or vertically."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val vertical = arguments.getRequired("vertical", CommandArgumentType.Boolean)
        return FlipTask(vertical, maxFileSize)
    }
}
