package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.GifTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

object GifCommand : FileCommand(
    CommandArgumentInfo(
        key = "transcode",
        aliases = setOf("t"),
        description = "Forces transcoding to GIF instead of just changing the file extension.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
) {

    override val name: String = "gif"
    override val description: String = "Converts media to a GIF file."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val forceTranscode = arguments.getRequired("transcode", CommandArgumentType.Boolean)
        return GifTask(forceTranscode, maxFileSize)
    }
}
