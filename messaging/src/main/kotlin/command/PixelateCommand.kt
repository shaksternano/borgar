package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.PixelateTask
import com.shakster.borgar.messaging.event.CommandEvent

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
