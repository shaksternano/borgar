package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.exception.MissingArgumentException
import io.github.shaksternano.borgar.core.io.task.CropTask
import io.github.shaksternano.borgar.core.io.task.FileTask

object CropCommand : FileCommand(
    CommandArgumentInfo(
        key = "top",
        aliases = setOf("t"),
        description = "Top crop ratio.",
        type = CommandArgumentType.DOUBLE,
        required = false,
        defaultValue = 0.0,
        validator = RangeValidator.ZERO_TO_ONE,
    ),
    CommandArgumentInfo(
        key = "bottom",
        aliases = setOf("b"),
        description = "Bottom crop ratio.",
        type = CommandArgumentType.DOUBLE,
        required = false,
        defaultValue = 0.0,
        validator = RangeValidator.ZERO_TO_ONE,
    ),
    CommandArgumentInfo(
        key = "left",
        aliases = setOf("l"),
        description = "Left crop ratio.",
        type = CommandArgumentType.DOUBLE,
        required = false,
        defaultValue = 0.0,
        validator = RangeValidator.ZERO_TO_ONE,
    ),
    CommandArgumentInfo(
        key = "right",
        aliases = setOf("r"),
        description = "Right crop ratio.",
        type = CommandArgumentType.DOUBLE,
        required = false,
        defaultValue = 0.0,
        validator = RangeValidator.ZERO_TO_ONE,
    ),
) {

    override val name: String = "crop"
    override val description: String = "Crops media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val topRatio = getRequiredArgument("top", CommandArgumentType.DOUBLE, arguments)
        val bottomRatio = getRequiredArgument("bottom", CommandArgumentType.DOUBLE, arguments)
        val leftRatio = getRequiredArgument("left", CommandArgumentType.DOUBLE, arguments)
        val rightRatio = getRequiredArgument("right", CommandArgumentType.DOUBLE, arguments)
        return if (topRatio == 0.0 && bottomRatio == 0.0 && leftRatio == 0.0 && rightRatio == 0.0) {
            throw MissingArgumentException("At least one crop ratio must be specified.")
        } else {
            CropTask(topRatio, bottomRatio, leftRatio, rightRatio, maxFileSize)
        }
    }
}
