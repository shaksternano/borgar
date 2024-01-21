package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.AutoCropTask
import io.github.shaksternano.borgar.core.io.task.FileTask
import java.awt.Color

object AutoCropCommand : FileCommand(
    CommandArgumentInfo(
        key = "tolerance",
        aliases = setOf("t"),
        description = "Background crop colour tolerance.",
        type = CommandArgumentType.DOUBLE,
        required = false,
        defaultValue = 0.2,
        validator = RangeValidator.ZERO_TO_ONE,
    ),
    CommandArgumentInfo(
        key = "rgb",
        description = "Background color to crop out. By default it is the color of the top left pixel.",
        type = CommandArgumentType.INTEGER,
        required = false,
    ),
    CommandArgumentInfo(
        key = "first",
        aliases = setOf("f"),
        description = "Whether to only check the background in the first frame or not.",
        type = CommandArgumentType.BOOLEAN,
        required = false,
        defaultValue = false,
    ),
) {

    override val name: String = "autocrop"
    override val description: String = "Automatically crops out background color."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val cropColor = arguments.getOptional("rgb", CommandArgumentType.INTEGER)
            ?.let { Color(it) }
        val tolerance = arguments.getRequired("tolerance", CommandArgumentType.DOUBLE)
        val onlyCheckFirst = arguments.getRequired("onlycheckfirst", CommandArgumentType.BOOLEAN)
        return AutoCropTask(cropColor, tolerance, onlyCheckFirst, maxFileSize)
    }
}
