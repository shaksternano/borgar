package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.exception.MissingArgumentException
import io.github.shaksternano.borgar.core.io.task.CropTask
import io.github.shaksternano.borgar.core.io.task.FileTask

object CropCommand : FileCommand(
    CommandArgumentInfo(
        key = "top",
        description = "Top crop ratio.",
        type = CommandArgumentType.DOUBLE,
        required = false,
        defaultValue = 0.0,
        validator = ZERO_TO_ONE_VALIDATOR,
    ),
    CommandArgumentInfo(
        key = "bottom",
        description = "Bottom crop ratio.",
        type = CommandArgumentType.DOUBLE,
        required = false,
        defaultValue = 0.0,
        validator = ZERO_TO_ONE_VALIDATOR,
    ),
    CommandArgumentInfo(
        key = "left",
        description = "Left crop ratio.",
        type = CommandArgumentType.DOUBLE,
        required = false,
        defaultValue = 0.0,
        validator = ZERO_TO_ONE_VALIDATOR,
    ),
    CommandArgumentInfo(
        key = "right",
        description = "Right crop ratio.",
        type = CommandArgumentType.DOUBLE,
        required = false,
        defaultValue = 0.0,
        validator = ZERO_TO_ONE_VALIDATOR,
    ),
) {

    override val name: String = "crop"
    override val description: String = "Crops media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val topRatio = getRequiredArgument("top", CommandArgumentType.DOUBLE, arguments, event)
        val bottomRatio = getRequiredArgument("bottom", CommandArgumentType.DOUBLE, arguments, event)
        val leftRatio = getRequiredArgument("left", CommandArgumentType.DOUBLE, arguments, event)
        val rightRatio = getRequiredArgument("right", CommandArgumentType.DOUBLE, arguments, event)
        return if (topRatio == 0.0 && bottomRatio == 0.0 && leftRatio == 0.0 && rightRatio == 0.0) {
            throw MissingArgumentException("At least one crop ratio must be specified.")
        } else {
            CropTask(topRatio, bottomRatio, leftRatio, rightRatio, maxFileSize)
        }
    }
}
