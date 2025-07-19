package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.task.FileTask
import io.github.shaksternano.borgar.core.task.UrlFileTask
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import io.github.shaksternano.borgar.messaging.util.getEmojiUrls
import io.github.shaksternano.borgar.messaging.util.searchOrThrow

object EmojiImageCommand : FileCommand(
    CommandArgumentInfo(
        key = "emoji",
        description = "The emoji to get the image of.",
        type = CommandArgumentType.String,
        required = false,
    ),
    inputRequirement = InputRequirement.NONE,
) {

    override val name: String = "emojiimage"
    override val aliases: Set<String> = setOf("emoji")
    override val description: String = "Gets the image of an emoji."

    override suspend fun createTask(arguments: CommandArguments, event: CommandEvent, maxFileSize: Long): FileTask {
        val messageIntersection = event.asMessageIntersection(arguments)
        val emojis = arguments.getDefaultStringOrEmpty()
        val emojiUrls = messageIntersection.searchOrThrow("No emojis found.") { message ->
            val urls = message.getEmojiUrls()
            val filteredUrls = if (message == messageIntersection) {
                urls.filter { (mention, _) ->
                    emojis.contains(mention)
                }
            } else {
                urls
            }
            filteredUrls.values.ifEmpty {
                null
            }
        }
        return UrlFileTask(emojiUrls)
    }
}
