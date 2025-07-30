package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.ReduceFpsTask
import com.shakster.borgar.messaging.event.CommandEvent

object ReduceFpsCommand : FileCommand(
    CommandArgumentInfo(
        key = "fpsreductionratio",
        aliases = setOf("ratio"),
        description = "FPS reduction multiplier.",
        type = CommandArgumentType.Double,
        required = false,
        defaultValue = 2.0,
        validator = GreaterThanOneValidator,
    )
) {

    override val name: String = "reducefps"
    override val aliases: Set<String> = setOf("redfps")
    override val description: String = "Reduces the FPS of a media file."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val fpsReductionRatio = arguments.getRequired("fpsreductionratio", CommandArgumentType.Double)
        return ReduceFpsTask(fpsReductionRatio, maxFileSize)
    }
}
