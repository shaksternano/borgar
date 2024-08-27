package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.PixelateTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

object PixelateCommand : FileCommand(
    CommandArgumentInfo(
        key = "pixelationmultiplier",
        aliases = setOf("pm"),
        description = "Pixelation multiplier.",
        type = CommandArgumentType.Double,
        validator = GreaterThanOneValidator,
    ),
) {

    override val name: String = "pixelate"
    override val aliases: Set<String> = setOf("pixel")
    override val description: String = "Pixelates media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val pixelationMultiplier = arguments.getRequired("pixelationmultiplier", CommandArgumentType.Double)
        return PixelateTask(pixelationMultiplier, maxFileSize)
    }
}
