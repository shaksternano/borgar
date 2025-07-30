package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.GifTask
import com.shakster.borgar.messaging.event.CommandEvent

object GifCommand : FileCommand(
    CommandArgumentInfo(
        key = "transcode",
        aliases = setOf("t"),
        description = "Forces transcoding to GIF instead of changing the file extension to .gif.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
    CommandArgumentInfo(
        key = "changeextension",
        aliases = setOf("e"),
        description = "Forces changing the file extension to .gif instead of transcoding to GIF.",
        type = CommandArgumentType.Boolean,
        required = false,
        defaultValue = false,
    ),
) {

    override val name: String = "gif"
    override val description: String = "Converts media to a GIF file. " +
        "Static images will just have their file extension changed to .gif."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val forceTranscode = arguments.getRequired("transcode", CommandArgumentType.Boolean)
        val forceRename = arguments.getRequired("changeextension", CommandArgumentType.Boolean)
        return GifTask(
            forceTranscode = forceTranscode,
            forceRename = forceRename,
            maxFileSize = maxFileSize,
        )
    }
}
