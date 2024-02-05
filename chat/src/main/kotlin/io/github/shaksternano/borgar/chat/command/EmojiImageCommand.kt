package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.getFirstEmojiUrl
import io.github.shaksternano.borgar.chat.util.search
import io.github.shaksternano.borgar.core.exception.FailedOperationException
import io.github.shaksternano.borgar.core.io.task.FileTask
import io.github.shaksternano.borgar.core.io.task.UrlFileTask

object EmojiImageCommand : FileCommand(
    CommandArgumentInfo(
        key = "emoji",
        description = "The emoji to get the image of.",
        type = CommandArgumentType.STRING,
        required = false,
    ),
    requireInput = false,
) {

    override val name: String = "emoji"
    override val description: String = "Gets the image of an emoji."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val emojiUrl = event.asMessageIntersection(arguments)
            .search(CommandMessageIntersection::getFirstEmojiUrl)
            ?: throw FailedOperationException("No emoji found.")
        return UrlFileTask(emojiUrl)
    }
}
