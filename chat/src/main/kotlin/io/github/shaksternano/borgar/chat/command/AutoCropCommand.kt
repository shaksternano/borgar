package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.AutoCropTask
import io.github.shaksternano.borgar.core.io.task.FileTask
import java.awt.Color

object AutoCropCommand : FileCommand(
    CommandArgumentInfo(
        key = "tolerance",
        description = "Background crop colour tolerance.",
        type = SimpleCommandArgumentType.DOUBLE,
        required = false,
        defaultValue = 0.2,
        validator = ZERO_TO_ONE_VALIDATOR,
    ),
    CommandArgumentInfo(
        key = "rgb",
        description = "Background color to crop out. By default it is the color of the top left pixel.",
        type = SimpleCommandArgumentType.LONG,
        required = false,
    ),
    CommandArgumentInfo(
        key = "first",
        description = "Whether to only check the background in the first frame or not.",
        type = SimpleCommandArgumentType.BOOLEAN,
        required = false,
        defaultValue = false,
    ),
) {

    override val name: String = "autocrop"
    override val description: String = "Automatically crops out background color."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val cropColor =
            getOptionalArgument("rgb", SimpleCommandArgumentType.LONG, arguments, event)?.let { Color(it.toInt()) }
        val tolerance = getRequiredArgument("tolerance", SimpleCommandArgumentType.DOUBLE, arguments, event)
        val onlyCheckFirst = getRequiredArgument("onlycheckfirst", SimpleCommandArgumentType.BOOLEAN, arguments, event)
        return AutoCropTask(cropColor, tolerance, onlyCheckFirst, maxFileSize)
    }
}
