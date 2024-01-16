package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.FlipTask

object FlipCommand : FileCommand(
    CommandArgumentInfo(
        key = "v",
        description = "Whether to flip vertically or not.",
        type = SimpleCommandArgumentType.BOOLEAN,
        required = false,
        defaultValue = false,
    ),
) {

    override val name: String = "flip"
    override val description: String = "Flips media horizontally or vertically."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val vertical = getRequiredArgument("v", SimpleCommandArgumentType.BOOLEAN, arguments, event)
        return FlipTask(vertical, maxFileSize)
    }
}
