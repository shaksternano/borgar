package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.RotateTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import java.awt.Color

object RotateCommand : FileCommand(
    CommandArgumentInfo(
        key = "degrees",
        description = "Degrees to rotate by.",
        type = CommandArgumentType.Double,
        defaultValue = 90.0,
        required = false,
    ),
    CommandArgumentInfo(
        key = "backgroundrgb",
        aliases = setOf("background", "bg"),
        description = "Background RGB color to fill in the empty space. By default it is transparent.",
        type = CommandArgumentType.Integer,
        required = false,
    ),
) {

    override val name: String = "rotate"
    override val description: String = "Rotates media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val degrees = arguments.getRequired("degrees", CommandArgumentType.Double)
        val backgroundRgb = arguments.getOptional("backgroundrgb", CommandArgumentType.Integer)
        val backgroundColor = backgroundRgb?.let { Color(it) }
        return RotateTask(degrees, backgroundColor, maxFileSize)
    }
}
