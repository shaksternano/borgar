package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.PixelateTask

object PixelateCommand : FileCommand(
    CommandArgumentInfo(
        key = "pixelationmultiplier",
        aliases = setOf("pm"),
        description = "Pixelation multiplier.",
        type = CommandArgumentType.DOUBLE,
        validator = GreaterThanOneValidator,
    ),
) {

    override val name: String = "pixelate"
    override val aliases: Set<String> = setOf("pixel")
    override val description: String = "Pixelates media."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val pixelationMultiplier = arguments.getRequired("pixelationmultiplier", CommandArgumentType.DOUBLE)
        return PixelateTask(pixelationMultiplier, maxFileSize)
    }
}
