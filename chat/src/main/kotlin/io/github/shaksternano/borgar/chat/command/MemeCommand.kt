package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.getEmojiAndUrlDrawables
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.MemeTask

object MemeCommand : FileCommand(
    CommandArgumentInfo(
        key = "text",
        description = "The text to put on the top of the image.",
        type = CommandArgumentType.STRING,
    ),
    CommandArgumentInfo(
        key = "bottom",
        description = "The text to put on the bottom of the image.",
        type = CommandArgumentType.STRING,
        required = false,
    ),
) {

    override val name: String = "meme"
    override val description: String = "Adds impact font text to a media file."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        MemeTask(
            topWords = arguments.getDefaultStringOrEmpty(),
            bottomWords = arguments.getStringOrEmpty("bottom"),
            nonTextParts = event.asMessageIntersection(arguments).getEmojiAndUrlDrawables(),
            maxFileSize = maxFileSize,
        )
}
