package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.io.task.DemotivateTask
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import io.github.shaksternano.borgar.messaging.util.getEmojiAndUrlDrawables

object DemotivateCommand : FileCommand(
    CommandArgumentInfo(
        key = "text",
        description = "The text to put on the image",
        type = CommandArgumentType.String,
    ),
    CommandArgumentInfo(
        key = "subtext",
        aliases = setOf("sub"),
        description = "The subtext to put on the image",
        type = CommandArgumentType.String,
        required = false,
    )
) {

    override val name: String = "demotiv"
    override val description: String = "Puts image in demotivate meme."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val text = arguments.getDefaultStringOrEmpty()
        val subText = arguments.getStringOrEmpty("subtext")
        val messageIntersection = event.asMessageIntersection(arguments)
        return DemotivateTask(
            text = formatMentions(text, messageIntersection),
            subText = formatMentions(subText, messageIntersection),
            nonTextParts = messageIntersection.getEmojiAndUrlDrawables(),
            maxFileSize = maxFileSize,
        )
    }
}
