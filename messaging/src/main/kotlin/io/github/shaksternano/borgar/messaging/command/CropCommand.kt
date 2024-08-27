package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.CropTask
import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import io.github.shaksternano.borgar.messaging.exception.MissingArgumentException

object CropCommand : FileCommand(
    CommandArgumentInfo(
        key = "top",
        aliases = setOf("t"),
        description = "Top crop ratio.",
        type = CommandArgumentType.Double,
        required = false,
        defaultValue = 0.0,
        validator = RangeValidator.ZERO_TO_ONE,
    ),
    CommandArgumentInfo(
        key = "bottom",
        aliases = setOf("b"),
        description = "Bottom crop ratio.",
        type = CommandArgumentType.Double,
        required = false,
        defaultValue = 0.0,
        validator = RangeValidator.ZERO_TO_ONE,
    ),
    CommandArgumentInfo(
        key = "left",
        aliases = setOf("l"),
        description = "Left crop ratio.",
        type = CommandArgumentType.Double,
        required = false,
        defaultValue = 0.0,
        validator = RangeValidator.ZERO_TO_ONE,
    ),
    CommandArgumentInfo(
        key = "right",
        aliases = setOf("r"),
        description = "Right crop ratio.",
        type = CommandArgumentType.Double,
        required = false,
        defaultValue = 0.0,
        validator = RangeValidator.ZERO_TO_ONE,
    ),
) {

    override val name: String = "crop"
    override val description: String = "Crops media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val topRatio = arguments.getRequired("top", CommandArgumentType.Double)
        val bottomRatio = arguments.getRequired("bottom", CommandArgumentType.Double)
        val leftRatio = arguments.getRequired("left", CommandArgumentType.Double)
        val rightRatio = arguments.getRequired("right", CommandArgumentType.Double)
        return if (topRatio == 0.0 && bottomRatio == 0.0 && leftRatio == 0.0 && rightRatio == 0.0) {
            throw MissingArgumentException("At least one crop ratio must be specified.")
        } else {
            CropTask(topRatio, bottomRatio, leftRatio, rightRatio, maxFileSize)
        }
    }
}
