package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.DemotivateTask
import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.messaging.event.CommandEvent
import com.shakster.borgar.messaging.exception.MissingArgumentException
import com.shakster.borgar.messaging.util.getEmojiAndUrlDrawables

object DemotivateCommand : FileCommand(
    CommandArgumentInfo(
        key = "text",
        description = "The text to put on the image",
        type = CommandArgumentType.String,
        required = false,
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
        if (text.isBlank() && subText.isBlank()) {
            throw MissingArgumentException("No text was provided.")
        }
        val messageIntersection = event.asMessageIntersection(arguments)
        return DemotivateTask(
            text = formatMentions(text, messageIntersection),
            subText = formatMentions(subText, messageIntersection),
            nonTextParts = messageIntersection.getEmojiAndUrlDrawables(),
            maxFileSize = maxFileSize,
        )
    }
}
