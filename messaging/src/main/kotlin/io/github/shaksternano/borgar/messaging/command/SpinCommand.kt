package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.SpinTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import java.awt.Color

object SpinCommand : FileCommand(
    CommandArgumentInfo(
        key = "speed",
        description = "The spin speed.",
        type = CommandArgumentType.Double,
        required = false,
        defaultValue = 1.0
    ),
    CommandArgumentInfo(
        key = "backgroundrgb",
        aliases = setOf("background", "bg"),
        description = "Background RGB color to fill in the empty space. By default it is transparent.",
        type = CommandArgumentType.Integer,
        required = false,
    ),
) {

    override val name: String = "spin"
    override val description: String = "Spins a media file."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val spinSpeed = arguments.getRequired("speed", CommandArgumentType.Double)
        val backgroundRgb = arguments.getOptional("backgroundrgb", CommandArgumentType.Integer)
        val backgroundColor = backgroundRgb?.let { Color(it) }
        return SpinTask(spinSpeed, backgroundColor, maxFileSize)
    }
}
