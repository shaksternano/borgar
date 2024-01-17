package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.getEmojiDrawables
import io.github.shaksternano.borgar.core.io.task.DemotivateTask
import io.github.shaksternano.borgar.core.io.task.FileTask

object DemotivateCommand : FileCommand(
    CommandArgumentInfo(
        key = "text",
        description = "The text to put on the image",
        type = CommandArgumentType.STRING,
        required = false,
    ),
    CommandArgumentInfo(
        key = "subtext",
        aliases = setOf("sub"),
        description = "The subtext to put on the image",
        type = CommandArgumentType.STRING,
        required = false,
    )
) {

    override val name: String = "demotiv"
    override val description: String = "Puts image in demotivate meme."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask =
        DemotivateTask(
            arguments.getDefaultStringOrEmpty(),
            arguments.getStringOrEmpty("subtext"),
            event.asMessageIntersection(arguments).getEmojiDrawables(),
            maxFileSize,
        )
}
