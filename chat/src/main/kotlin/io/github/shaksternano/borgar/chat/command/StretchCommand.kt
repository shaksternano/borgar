package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.StretchTask

object StretchCommand : FileCommand(
    CommandArgumentInfo(
        key = "widthmultiplier",
        aliases = setOf("w", "width"),
        description = "Width stretch multiplier.",
        type = CommandArgumentType.Double,
        required = false,
        defaultValue = 2.0,
        validator = PositiveDoubleValidator,
    ),
    CommandArgumentInfo(
        key = "heightmultiplier",
        aliases = setOf("h", "height"),
        description = "Height stretch multiplier.",
        type = CommandArgumentType.Double,
        required = false,
        defaultValue = 1.0,
        validator = PositiveDoubleValidator,
    ),
    CommandArgumentInfo(
        key = "raw",
        description = "Whether to stretch without extra processing.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
) {

    override val name: String = "stretch"
    override val description: String = "Stretches media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val widthMultiplier = arguments.getRequired("widthmultiplier", CommandArgumentType.Double)
        val heightMultiplier = arguments.getRequired("heightmultiplier", CommandArgumentType.Double)
        val raw = arguments.getRequired("raw", CommandArgumentType.Boolean)
        return StretchTask(widthMultiplier, heightMultiplier, raw, maxFileSize)
    }
}
