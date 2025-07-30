package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.task.FileTask
import com.shakster.borgar.core.task.MemeTask
import com.shakster.borgar.messaging.event.CommandEvent
import com.shakster.borgar.messaging.exception.MissingArgumentException
import com.shakster.borgar.messaging.util.getEmojiAndUrlDrawables

object MemeCommand : FileCommand(
    CommandArgumentInfo(
        key = "text",
        description = "The text to put on the top of the image.",
        type = CommandArgumentType.String,
        required = false,
    ),
    CommandArgumentInfo(
        key = "bottom",
        description = "The text to put on the bottom of the image.",
        type = CommandArgumentType.String,
        required = false,
    ),
) {

    override val name: String = "meme"
    override val description: String = "Adds impact font text to a media file."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val topText = arguments.getDefaultStringOrEmpty()
        val bottomText = arguments.getStringOrEmpty("bottom")
        if (topText.isBlank() && bottomText.isBlank()) {
            throw MissingArgumentException("No text was provided.")
        }
        val messageIntersection = event.asMessageIntersection(arguments)
        return MemeTask(
            topText = formatMentions(topText, messageIntersection),
            bottomText = formatMentions(bottomText, messageIntersection),
            nonTextParts = messageIntersection.getEmojiAndUrlDrawables(),
            maxFileSize = maxFileSize,
        )
    }
}
