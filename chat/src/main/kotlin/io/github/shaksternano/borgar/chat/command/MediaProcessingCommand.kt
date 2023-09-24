package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.MediaProcessingTask
import io.github.shaksternano.borgar.core.media.MediaProcessConfig

abstract class MediaProcessingCommand : FileCommand() {

    final override fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        MediaProcessingTask(
            maxFileSize,
            createConfig(arguments, event),
        )

    abstract fun createConfig(arguments: CommandArguments, event: CommandEvent): MediaProcessConfig
}
