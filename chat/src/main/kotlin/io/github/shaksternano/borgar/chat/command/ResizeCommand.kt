package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.StretchTask

object ResizeCommand : FileCommand(
    CommandArgumentInfo(
        key = "resizemultiplier",
        aliases = setOf("r", "resize"),
        description = "Resize multiplier.",
        type = CommandArgumentType.DOUBLE,
        validator = Validator,
    ),
    CommandArgumentInfo(
        key = "raw",
        description = "Whether to stretch without extra processing.",
        type = CommandArgumentType.BOOLEAN,
        required = false,
        defaultValue = false,
    ),
) {

    override val name: String = "resize"
    override val description: String = "Resizes media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val resizeMultiplier = arguments.getRequired("resizemultiplier", CommandArgumentType.DOUBLE)
        val raw = arguments.getRequired("raw", CommandArgumentType.BOOLEAN)
        return StretchTask(resizeMultiplier, resizeMultiplier, raw, maxFileSize)
    }

    private object Validator : MinValueValidator<Double>(
        minValue = 0.0,
    ) {

        override fun validate(value: Double): Boolean = value != 1.0 && value > 0.0

        override fun errorMessage(value: Double, key: String): String =
            if (value == 1.0) {
                "The value for **$key** must not be 1."
            } else {
                "The value for **$key** must be positive."
            }
    }
}