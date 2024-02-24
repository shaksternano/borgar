package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.SpinTask
import java.awt.Color

object SpinCommand : FileCommand(
    CommandArgumentInfo(
        key = "speed",
        description = "The spin speed.",
        type = CommandArgumentType.DOUBLE,
        required = false,
        defaultValue = 1.0
    ),
    CommandArgumentInfo(
        key = "backgroundrgb",
        aliases = setOf("background", "bg"),
        description = "Background RGB color to fill in the empty space. By default it is transparent.",
        type = CommandArgumentType.INTEGER,
        required = false,
    ),
) {

    override val name: String = "spin"
    override val description: String = "Spins a media file."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val spinSpeed = arguments.getRequired("speed", CommandArgumentType.DOUBLE)
        val backgroundRgb = arguments.getOptional("backgroundrgb", CommandArgumentType.INTEGER)
        val backgroundColor = backgroundRgb?.let { Color(it) }
        return SpinTask(spinSpeed, backgroundColor, maxFileSize)
    }
}
