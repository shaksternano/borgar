package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.exception.MissingArgumentException
import io.github.shaksternano.borgar.core.io.task.CropTask
import io.github.shaksternano.borgar.core.io.task.FileTask

object CropCommand : FileCommand() {

    override val name: String = "crop"
    override val description: String = "Crops media."

    override val argumentInfo: Set<CommandArgumentInfo<*>> = setOf(
        CommandArgumentInfo(
            key = "top",
            description = "Top crop ratio.",
            type = SimpleCommandArgumentType.DOUBLE,
            required = false,
            defaultValue = 0.0,
            validator = ZERO_TO_ONE_VALIDATOR,
        ),
        CommandArgumentInfo(
            key = "bottom",
            description = "Bottom crop ratio.",
            type = SimpleCommandArgumentType.DOUBLE,
            required = false,
            defaultValue = 0.0,
            validator = ZERO_TO_ONE_VALIDATOR,
        ),
        CommandArgumentInfo(
            key = "left",
            description = "Left crop ratio.",
            type = SimpleCommandArgumentType.DOUBLE,
            required = false,
            defaultValue = 0.0,
            validator = ZERO_TO_ONE_VALIDATOR,
        ),
        CommandArgumentInfo(
            key = "right",
            description = "Right crop ratio.",
            type = SimpleCommandArgumentType.DOUBLE,
            required = false,
            defaultValue = 0.0,
            validator = ZERO_TO_ONE_VALIDATOR,
        ),
    )

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val topRatio = getRequiredArgument("top", SimpleCommandArgumentType.DOUBLE, arguments, event)
        val bottomRatio = getRequiredArgument("bottom", SimpleCommandArgumentType.DOUBLE, arguments, event)
        val leftRatio = getRequiredArgument("left", SimpleCommandArgumentType.DOUBLE, arguments, event)
        val rightRatio = getRequiredArgument("right", SimpleCommandArgumentType.DOUBLE, arguments, event)
        return if (topRatio == 0.0 && bottomRatio == 0.0 && leftRatio == 0.0 && rightRatio == 0.0) {
            throw MissingArgumentException("At least one crop ratio must be specified.")
        } else {
            CropTask(topRatio, bottomRatio, leftRatio, rightRatio, maxFileSize)
        }
    }
}
