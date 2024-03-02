package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.StretchTask

object ResizeCommand : FileCommand(
    CommandArgumentInfo(
        key = "resizemultiplier",
        aliases = setOf("r", "resize"),
        description = "Resize multiplier.",
        type = CommandArgumentType.Double,
        validator = ResizeValidator,
    ),
    CommandArgumentInfo(
        key = "raw",
        description = "Whether to stretch without extra processing.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
) {

    override val name: String = "resize"
    override val description: String = "Resizes media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val resizeMultiplier = arguments.getRequired("resizemultiplier", CommandArgumentType.Double)
        val raw = arguments.getRequired("raw", CommandArgumentType.Boolean)
        return StretchTask(resizeMultiplier, resizeMultiplier, raw, maxFileSize)
    }
}

private object ResizeValidator : MinValueValidator<Double>(
    minValue = 0.0,
) {

    override fun validate(value: Double): Boolean = value != 1.0 && value > 0.0

    override fun errorMessage(value: Double, key: String): String =
        if (value == 1.0) {
            "The argument **$key** must not be 1."
        } else {
            "The argument **$key** must be positive."
        }
}
