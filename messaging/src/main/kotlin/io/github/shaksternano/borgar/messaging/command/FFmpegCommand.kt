package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.io.task.FFmpegTask
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent

object FFmpegCommand : FileCommand(
    CommandArgumentInfo(
        key = "arguments",
        description = "The FFmpeg arguments.",
        type = CommandArgumentType.String,
        required = false,
    ),
) {

    override val name: String = "ffmpeg"
    override val description: String = "Runs an FFmpeg command. The input is specified automatically."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val ffmpegArguments = arguments.getDefaultStringOrEmpty()
        return FFmpegTask(ffmpegArguments)
    }
}
