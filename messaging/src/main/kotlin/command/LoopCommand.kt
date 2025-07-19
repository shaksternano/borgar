package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.LoopTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

object LoopCommand : FileCommand(
    CommandArgumentInfo(
        key = "loopcount",
        aliases = setOf("n"),
        description = "The number of times to loop the GIF."
            + " A value of -1 will make it loop forever.",
        type = CommandArgumentType.Integer,
        required = true,
        validator = RangeValidator(-1..65535),
    ),
) {

    override val name: String = "loop"
    override val description: String = "Changes the number of times a GIF loops."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val loopCount = arguments.getRequired("loopcount", CommandArgumentType.Integer)
        return LoopTask(loopCount)
    }
}
