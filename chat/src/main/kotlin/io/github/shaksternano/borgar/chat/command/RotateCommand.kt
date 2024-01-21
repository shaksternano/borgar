package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.RotateTask
import java.awt.Color

object RotateCommand : FileCommand(
    CommandArgumentInfo(
        key = "degrees",
        description = "Degrees to rotate by.",
        type = CommandArgumentType.DOUBLE,
        defaultValue = 90.0,
        required = false,
    ),
    CommandArgumentInfo(
        key = "backgroundrgb",
        aliases = setOf("bg"),
        description = "Background RGB color to fill in the empty space. By default it is transparent.",
        type = CommandArgumentType.INTEGER,
        required = false,
    ),
) {

    override val name: String = "rotate"
    override val description: String = "Rotates media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val degrees = arguments.getRequired("degrees", CommandArgumentType.DOUBLE)
        val backgroundRgb = arguments.getOptional("backgroundrgb", CommandArgumentType.INTEGER)
        val backgroundColor = backgroundRgb?.let { Color(it) }
        return RotateTask(degrees, backgroundColor, maxFileSize)
    }
}
